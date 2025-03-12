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
import org.homio.api.model.OptionModel;
import org.homio.api.model.endpoint.BaseDeviceEndpoint;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.ui.UI;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  private GpioEntity entity;
  private PinMode mode;
  private PullResistance pull;
  @Setter
  private IO instance;

  GpioPinEndpoint(GpioPin gpioPin, GpioEntity entity, GPIOService service) {
    super(gpioPin.getIcon(), "GPIO", entity.context(), entity, String.valueOf(gpioPin.getAddress()), false, EndpointType.bool);
    this.gpioPin = gpioPin;
    this.entity = entity;
    this.service = service;
    var gpioDto = findGpioDto();
    this.pull = gpioDto.pull;
    this.mode = gpioDto.mode;
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
      deleteVariableID();
    }
    getOrCreateVariable();

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
        fireReloadGpioPin(context);
      }
      return null;
    }).setValue(mode.name()).setOptions(modeSelectionList).setSeparatedText("field.pinMode");
    settingsBuilder.addSelectBox(getEntityID() + "_pull", (context, params) -> {
      String pull = params.getString("value");
      var pullResistance = PullResistance.valueOf(pull);

      if (pullResistance != this.pull) {
        this.pull = pullResistance;
        fireReloadGpioPin(context);
      }
      return null;
    }).setValue(pull.name()).setOptions(OptionModel.enumList(PullResistance.class)).setSeparatedText("field.pullResistance");
    return settingsBuilder;
  }

  private void fireReloadGpioPin(Context context) {
    this.service.createOrUpdateState(this, true);
    GpioEntity model = context.db().getRequire(entity.getEntityID());
    updateGpioDto(model);
    this.entity = context.db().save(model);
  }

  private void updateGpioDto(GpioEntity entity) {
    List<GpioDto> gpioList = entity.getJsonDataList("gpio", GpioDto.class);
    boolean found = false;
    for (GpioDto gpioDto : gpioList) {
      if (gpioDto.address == gpioPin.getAddress()) {
        gpioDto.pull = pull;
        gpioDto.mode = mode;
        found = true;
        break;
      }
    }
    if (!found) {
      gpioList.add(new GpioDto(gpioPin.getAddress(), pull, mode));
    }
    entity.setJsonDataObject("gpio", gpioList);
  }

  private GpioDto findGpioDto() {
    List<GpioDto> gpioList = entity.getJsonDataList("gpio", GpioDto.class);
    for (GpioDto gpioDto : gpioList) {
      if (gpioDto.address == gpioPin.getAddress()) {
        return gpioDto;
      }
    }
    return new GpioDto(gpioPin.getAddress(), PullResistance.PULL_DOWN, PinMode.DIGITAL_INPUT);
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
  }
}
