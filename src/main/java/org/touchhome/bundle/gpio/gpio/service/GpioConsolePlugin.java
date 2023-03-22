package org.touchhome.bundle.gpio.gpio.service;

import com.pi4j.io.gpio.digital.PullResistance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.console.ConsolePluginTable;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorMatch;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorRef;
import org.touchhome.bundle.gpio.gpio.GPIOService;
import org.touchhome.bundle.gpio.gpio.GpioPin;
import org.touchhome.bundle.gpio.gpio.GpioState;
import org.touchhome.bundle.gpio.gpio.mode.PinMode;

@RequiredArgsConstructor
public class GpioConsolePlugin implements ConsolePluginTable<GpioConsolePlugin.GpioPluginEntity> {

    @Getter
    private final EntityContext entityContext;
    private final GPIOService gpioService;

    @Override
    public String getParentTab() {
        return "GPIO";
    }

    @Override
    public Collection<GpioPluginEntity> getValue() {
        List<GpioPluginEntity> list = new ArrayList<>();

        for (GpioState gpioState : gpioService.getState().values()) {
            GpioPin gpioPin = gpioState.getGpioPin();
            GpioPluginEntity gpioPluginEntity = new GpioPluginEntity();
            gpioPluginEntity.setAddress(gpioPin.getAddress());
            gpioPluginEntity.setName(gpioPin.getName());
            gpioPluginEntity.setDescription(gpioPin.getDescription());
            gpioPluginEntity.setSupportedModes(gpioPin.getSupportModes());
            gpioPluginEntity.setMode(gpioState.getPinMode());
            gpioPluginEntity.setColor(gpioPin.getColor());
            gpioPluginEntity.setValue(gpioState.getLastState());
            gpioPluginEntity.setPullResistance(gpioState.getPull());
            list.add(gpioPluginEntity);
        }

        Collections.sort(list);
        return list;
    }

    @Override
    public int order() {
        return 2000;
    }

    @Override
    public String getName() {
        return "gpio";
    }

    @Override
    public Class<GpioPluginEntity> getEntityClass() {
        return GpioPluginEntity.class;
    }

    @Getter
    @Setter
    public static class GpioPluginEntity implements HasEntityIdentifier, Comparable<GpioPluginEntity> {

        @UIField(order = 1, label = "№")
        private int address;

        @UIField(order = 2)
        @UIFieldColorRef("color")
        private String name;

        @UIField(order = 3)
        private String description;

        @UIField(order = 6, label = "Mode")
        private PinMode mode;

        @UIField(order = 7, label = "Occupied")
        private String occupied;

        @UIField(order = 8, label = "Supported modes", type = UIFieldType.Chips)
        private Set<PinMode> supportedModes;

        @UIField(order = 9, label = "Pull Resistance")
        private PullResistance pullResistance;

        @UIField(order = 11)
        @UIFieldColorMatch(value = "HIGH", color = "#1F8D2D")
        @UIFieldColorMatch(value = "LOW", color = "#B22020")
        private State value;

        private String color;

        public String getEntityID() {
            return name;
        }

    /* @UIContextMenuAction("ACTION.PULL_DOWN_RESISTOR")
    public ActionResponseModel pullDownResistor(EntityContext entityContext, GpioPluginEntity gpioPluginEntity) {
      return setPullResistance(gpioPluginEntity, entityContext, PullResistance.PULL_DOWN);
    }

    @UIContextMenuAction("ACTION.PULL_UP_RESISTOR")
    public ActionResponseModel pullUpResistor(EntityContext entityContext, GpioPluginEntity gpioPluginEntity) {
      return setPullResistance(gpioPluginEntity, entityContext, PullResistance.PULL_UP);
    }

    @UIContextMenuAction("ACTION.SET_HIGH")
    public ActionResponseModel setHighState(EntityContext entityContext, GpioPluginEntity gpioPluginEntity) {
      return setPinState(gpioPluginEntity, entityContext, PinState.HIGH);
    }

    @UIContextMenuAction("ACTION.SET_LOW")
    public ActionResponseModel setLowState(EntityContext entityContext, GpioPluginEntity gpioPluginEntity) {
      return setPinState(gpioPluginEntity, entityContext, PinState.LOW);
    }

    private ActionResponseModel setPullResistance(GpioPluginEntity gpioPluginEntity, EntityContext entityContext,
        PullResistance PullResistance) {
      RaspberryGpioPin gpioPin = RaspberryGpioPin.fromValue(gpioPluginEntity.getRaspiPin());
      entityContext.getBean(RaspberryGPIOService.class).setPullResistance(gpioPin, PullResistance);
      return ActionResponseModel.showSuccess("success");
    }

    private ActionResponseModel setPinState(GpioPluginEntity gpioPluginEntity, EntityContext entityContext,
        PinState pinState) {
      RaspberryGpioPin gpioPin = RaspberryGpioPin.fromValue(gpioPluginEntity.getRaspiPin());
      entityContext.getBean(RaspberryGPIOService.class).setValue(gpioPin, pinState);
      return ActionResponseModel.showSuccess("success");
    }*/

        @Override
        public int compareTo(@NotNull GpioPluginEntity o) {
            return this.name.compareTo(o.name);
        }
    }
}
