package org.homio.bundle.gpio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pi4j.plugin.mock.Mock;
import com.pi4j.plugin.pigpio.PiGpioPlugin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import lombok.Getter;
import lombok.Setter;
import org.homio.bundle.gpio.gpio.GPIOService;
import org.homio.bundle.gpio.gpio.GpioPinEntity;
import org.homio.bundle.gpio.gpio.GpioProviderIdModel;
import org.jetbrains.annotations.NotNull;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.entity.types.MicroControllerBaseEntity;
import org.homio.bundle.api.model.HasEntityIdentifier;
import org.homio.bundle.api.model.HasEntityLog;
import org.homio.bundle.api.model.OptionModel;
import org.homio.bundle.api.service.EntityService;
import org.homio.bundle.api.ui.UISidebarChildren;
import org.homio.bundle.api.ui.action.DynamicOptionLoader;
import org.homio.bundle.api.ui.field.UIField;
import org.homio.bundle.api.ui.field.UIFieldSlider;
import org.homio.bundle.api.ui.field.inline.UIFieldInlineEditEntities;
import org.homio.bundle.api.ui.field.inline.UIFieldInlineEntities;
import org.homio.bundle.api.ui.field.selection.UIFieldSelection;
import org.homio.bundle.api.util.BoardInfo;

@Entity
@UISidebarChildren(icon = "fas fa-keyboard", color = "#92BA1A")
public final class GpioEntity extends MicroControllerBaseEntity<GpioEntity>
    implements EntityService<GPIOService, GpioEntity>, HasEntityLog, HasEntityIdentifier {

    public static final String PREFIX = "gpio_";
    public static final String BOARD_TYPE = BoardInfo.revision == null ? "UNKNOWN" : GpioEntrypoint.readBoardType();

    private static final Map<String, GpioProviderIdModel> GPIO_PROVIDERS = new HashMap<>();

    static {
        GPIO_PROVIDERS.put("MOCK", new GpioProviderIdModel(
            Mock.DIGITAL_INPUT_PROVIDER_ID,
            Mock.DIGITAL_OUTPUT_PROVIDER_ID,
            Mock.PWM_PROVIDER_ID,
            Mock.ANALOG_INPUT_PROVIDER_ID,
            Mock.ANALOG_OUTPUT_PROVIDER_ID,
            Mock.SPI_PROVIDER_ID,
            Mock.SERIAL_PROVIDER_ID,
            Mock.I2C_PROVIDER_ID
        ));
        if (!BOARD_TYPE.equals("UNKNOWN")) {
            GPIO_PROVIDERS.put("RPI", new GpioProviderIdModel(
                PiGpioPlugin.DIGITAL_INPUT_PROVIDER_ID,
                PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID,
                PiGpioPlugin.PWM_PROVIDER_ID,
                null,
                null,
                PiGpioPlugin.SPI_PROVIDER_ID,
                PiGpioPlugin.SERIAL_PROVIDER_ID,
                PiGpioPlugin.I2C_PROVIDER_ID
            ));
        }
    }

    @Getter
    @Setter
    @UIField(order = 1000)
    @UIFieldInlineEntities(bg = "#1E5E611F")
    @UIFieldInlineEditEntities(bg = "#1E5E611F", addRowCondition = "return false", removeRowCondition = "return false")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "owner")
    @OrderBy("position asc")
    private Set<GpioPinEntity> gpioPinEntities;

    @JsonIgnore
    public GpioProviderIdModel getGpioProviderModel() {
        return GPIO_PROVIDERS.get(getGpioProvider());
    }

    @UIField(order = 4)
    @UIFieldSelection(SelectGpioProviders.class)
    public String getGpioProvider() {
        return getJsonData("prv", "MOCK");
    }

    public void setGpioProvider(String value) {
        setJsonData("prv", value);
    }

    @UIField(order = 4)
    @UIFieldSlider(min = 1, max = 120, step = 5, header = "S")
    public int getOneWireInterval() {
        return getJsonData("owi", 30);
    }

    public void setOneWireInterval(int value) {
        setJsonData("owi", value);
    }

    @Override
    public String getDefaultName() {
        return "Gpio";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public void logBuilder(EntityLogBuilder entityLogBuilder) {
        entityLogBuilder.addTopic("org.homio.bundle.gpio");
    }

    @Override
    public Class<GPIOService> getEntityServiceItemClass() {
        return GPIOService.class;
    }

    @Override
    public @NotNull GPIOService createService(@NotNull EntityContext entityContext) {
        return new GPIOService(entityContext, RaspberryGpioPin.getGpioPins(), this);
    }

    public static class SelectGpioProviders implements DynamicOptionLoader {

        @Override
        public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
            return OptionModel.list(GPIO_PROVIDERS.keySet());
        }
    }
}
