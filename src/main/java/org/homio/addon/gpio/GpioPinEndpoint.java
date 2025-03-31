package org.homio.addon.gpio;

import com.pi4j.io.IO;
import com.pi4j.io.gpio.digital.PullResistance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.WordUtils;
import org.homio.addon.gpio.mode.PinMode;
import org.homio.api.Context;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.model.endpoint.BaseDeviceEndpoint;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.ui.UI;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
@Getter
public class GpioPinEndpoint extends BaseDeviceEndpoint<GpioEntity> {

  private final Map<String, Consumer<State>> listeners = new HashMap<>();
  private final GpioPin gpioPin;
  private final GPIOService service;
  private final GpioEntity entity;
  private PinMode mode;
  private PullResistance pull;
  @Setter
  private IO instance;

  GpioPinEndpoint(GpioPin gpioPin, GpioEntity entity, GPIOService service) {
    super(gpioPin.getIcon(), "GPIO", entity.context(), entity, String.valueOf(gpioPin.getAddress()), false, EndpointType.bool);
    this.gpioPin = gpioPin;
    this.entity = entity;
    this.service = service;

    var dto = findGpioDto();
    this.pull = dto.pull;
    this.mode = dto.mode;
    setIcon(dto.icon);
    gpioPin.setName(dto.name);
    setUpdateHandler(state -> mode.getGpioModeFactory().setState(instance, state));
    // writable after updateHandler
    setWritable();
  }

  private void setWritable() {
    EndpointType prevEndpointType = getEndpointType();
    setWritable(mode == PinMode.DIGITAL_OUTPUT || mode == PinMode.ANALOG_OUTPUT);
    switch (mode) {
      case ANALOG_INPUT, ANALOG_OUTPUT -> setEndpointType(EndpointType.dimmer);
      case DIGITAL_INPUT, DIGITAL_OUTPUT -> setEndpointType(EndpointType.bool);
      case PWM -> setEndpointType(EndpointType.number);
    }
    if (getVariableID() != null && prevEndpointType != getEndpointType()) {
      recreateVariable();
    } else {
      getOrCreateVariable();
    }

    State value = getValue();
    if (getEndpointType() == EndpointType.bool) {
      if (!(value instanceof OnOffType)) {
        setValue(OnOffType.OFF, true);
      }
    } else if (getEndpointType() == EndpointType.number || getEndpointType() == EndpointType.dimmer) {
      if (!(value instanceof DecimalType)) {
        setValue(DecimalType.ZERO, true);
      }
    }
  }

  @Override
  public String getVariableGroupID() {
    return "gpio";
  }

  @Override
  public @NotNull String getName(boolean shortFormat) {
    return gpioPin.getName().replace("_", "");
  }

  @Override
  public @Nullable String getDescription() {
    String pullIcon = switch (pull) {
      case PULL_UP -> "<i class=\"fas fa-arrow-up\" style=\"color:" + UI.Color.GREEN + ";\"></i>";
      case PULL_DOWN -> "<i class=\"fas fa-arrow-down\" style=\"color:" + UI.Color.BLUE + ";\"></i>";
      case OFF -> "<i class=\"fas fa-ban\" style=\"color: " + UI.Color.WARNING + ";\"></i>";
    };

    String modeIcon = switch (mode) {
      case DIGITAL_INPUT -> "<i class=\"fas fa-sign-in-alt\" style=\"color: #d4a373;\"></i>";
      case DIGITAL_OUTPUT -> "<i class=\"fas fa-sign-out-alt\" style=\"color: #e76f51;\"></i>";
      case PWM -> "<i class=\"fas fa-wave-square\" style=\"color: #9b5de5;\"></i>";
      case ANALOG_INPUT -> "<i class=\"fas fa-sliders-h\" style=\"color: #8ecae6;\"></i>";
      case ANALOG_OUTPUT -> "<i class=\"fas fa-volume-up\" style=\"color: #b08968;\"></i>";
      case ONE_WIRE -> "<i class=\"fas fa-link\" style=\"color: #c77dff;\"></i>";
    };

    String modeStr = WordUtils.capitalizeFully(mode.name().replace('_', ' '));
    String pullStr = WordUtils.capitalizeFully("PULL " + pull.getName());
    return gpioPin.getDescription() +
           ". " + pullIcon + pullStr +
           ", " + modeIcon + modeStr;
  }

  public void setValueFromDevice(State lastState) {
    log.debug("Update state: '{}' for pin: '{}'", lastState, gpioPin.getName());
    setValue(lastState, true);
  }

  @Override
  public @Nullable UIInputBuilder createSettingsBuilder() {
    UIInputBuilder settingsBuilder = context().ui().inputBuilder();
    var modeSelectionList = OptionModel.enumList(PinMode.class, pinMode ->
      gpioPin.getSupportModes().contains(pinMode));
    settingsBuilder.addSelectBox(getEntityID() + "mode", (context, params) -> {
      String modeStr = params.getString("value");
      var mode = PinMode.valueOf(modeStr);

      if (mode != this.mode) {
        this.mode = mode;
        setWritable();
        fireReloadGpioPin();
      }
      return null;
    }).setValue(mode.name()).setOptions(modeSelectionList).setSeparatedText("field.pinMode");
    settingsBuilder.addSelectBox(getEntityID() + "_pull", (context, params) -> {
      String pull = params.getString("value");
      var pullResistance = PullResistance.valueOf(pull);

      if (pullResistance != this.pull) {
        this.pull = pullResistance;
        fireReloadGpioPin();
      }
      return null;
    }).setValue(pull.name()).setOptions(OptionModel.enumList(PullResistance.class)).setSeparatedText("field.pullResistance");
    settingsBuilder.addIconPicker(getEntityID() + "icon", getIcon().getIcon())
      .setActionHandler((context, params) ->
        updateIcon(context, new Icon(params.getString("value"), getIcon().getColor())))
      .setSeparatedText("field.icon");
    settingsBuilder.addColorPicker(getEntityID() + "color", getIcon().getColor())
      .setActionHandler((context, params) ->
        updateIcon(context, new Icon(getIcon().getIcon(), params.getString("value"))))
      .setSeparatedText("field.iconColor");
    settingsBuilder.addTextInput(getEntityID() + "name", gpioPin.getName(), true)
      .setRequireApply(true)
      .setActionHandler((context, params) -> {
        gpioPin.setName(params.getString("value"));
        updateEntityDto();
        // fire update variable name
        recreateVariable();
        return null;
      })
      .setSeparatedText("field.name");
    return settingsBuilder;
  }

  private void fireReloadGpioPin() {
    this.service.createOrUpdateState(this, true);
    updateEntityDto();
  }

  // delay save icon if a user wants to change it
  private ActionResponseModel updateIcon(Context context, Icon icon) {
    context.bgp().builder(getEntityID() + "-request-change-icon-color")
      .delay(Duration.ofSeconds(3))
      .execute(() -> {
        recreateVariable();
        setIcon(icon);
        updateEntityDto();
      });
    return null;
  }

  private void updateEntityDto() {
    var updated = entity.tryUpdateEntity(() -> {
      List<GpioDto> gpioList = entity.getJsonDataList("gpio", GpioDto.class);
      boolean found = false;
      for (GpioDto gpioDto : gpioList) {
        if (gpioDto.address == gpioPin.getAddress()) {
          gpioDto.pull = pull;
          gpioDto.mode = mode;
          gpioDto.name = gpioPin.getName();
          gpioDto.icon = getIcon();
          found = true;
          break;
        }
      }
      if (!found) {
        gpioList.add(new GpioDto(gpioPin.getAddress(), pull, mode, getIcon(), gpioPin.getName()));
      }
      entity.setJsonDataObject("gpio", gpioList);
    });
    if (updated) {
      context().db().save(entity);
    }
  }

  private GpioDto findGpioDto() {
    List<GpioDto> gpioList = entity.getJsonDataList("gpio", GpioDto.class);
    for (GpioDto gpioDto : gpioList) {
      if (gpioDto.address == gpioPin.getAddress()) {
        return gpioDto;
      }
    }
    return new GpioDto(gpioPin.getAddress(), PullResistance.PULL_DOWN, PinMode.DIGITAL_INPUT, getIcon(), gpioPin.getName());
  }

  @Override
  public String toString() {
    return gpioPin.getName();
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GpioDto {
    private int address;
    private PullResistance pull;
    private PinMode mode;
    private Icon icon;
    private String name;
  }
}
