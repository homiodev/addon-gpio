package org.touchhome.bundle.gpio.gpio.service;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.state.DecimalType;
import org.touchhome.bundle.api.state.OnOffType;
import org.touchhome.bundle.api.state.OnOffType.OnOffTypeEnum;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.api.workspace.BroadcastLock;
import org.touchhome.bundle.api.workspace.WorkspaceBlock;
import org.touchhome.bundle.api.workspace.scratch.MenuBlock;
import org.touchhome.bundle.api.workspace.scratch.MenuBlock.ServerMenuBlock;
import org.touchhome.bundle.api.workspace.scratch.Scratch3ExtensionBlocks;
import org.touchhome.bundle.gpio.GpioEntity;
import org.touchhome.bundle.gpio.GpioEntrypoint;
import org.touchhome.bundle.gpio.gpio.GPIOService;
import org.touchhome.bundle.gpio.gpio.GpioController.Mode;

@Getter
@Component
public class Scratch3GpioBlocks extends Scratch3ExtensionBlocks {

    private final MenuBlock.StaticMenuBlock<OnOffType.OnOffTypeEnum> menuOnOff;
    private final ServerMenuBlock menuDigitalInputPin;
    private final ServerMenuBlock menuDigitalOutputPin;
    private final ServerMenuBlock menuAnalogOutputPin;
    private final ServerMenuBlock menuInputPin;
    private final ServerMenuBlock menuDS18B20;
    private final ServerMenuBlock rpiIdMenu;

    public Scratch3GpioBlocks(EntityContext entityContext, GpioEntrypoint gpioEntrypoint) {
        super("#83be41", entityContext, gpioEntrypoint);

        this.rpiIdMenu = menuServerItems("rpiIdMenu", GpioEntity.class, "Raspberry");
        this.menuInputPin = menuServer("ipMenu", "rest/gpio/pin/" + Mode.input, "-").setDependency(this.rpiIdMenu);
        this.menuDigitalInputPin = menuServer("dipMenu", "rest/gpio/pin/" + Mode.digitalInput, "-").setDependency(this.rpiIdMenu);
        this.menuDigitalOutputPin = menuServer("dopMenu", "rest/gpio/pin/" + Mode.digitalOutput, "-").setDependency(this.rpiIdMenu);
        this.menuAnalogOutputPin = menuServer("aoMenu", "rest/gpio/pin/" + Mode.analogOutput, "-").setDependency(this.rpiIdMenu);
        this.menuOnOff = menuStatic("onOffMenu", OnOffTypeEnum.class, OnOffTypeEnum.On);
        this.menuDS18B20 = menuServer("ds18b20Menu", "rest/gpio/device/DS18B20", "DS18B20");

        blockCommand(0, "set_gpio", "Set [ONOFF] to pin [PIN] of [RPI]", this::writeDigitalPinCommand, block -> {
            block.addArgument("RPI", this.rpiIdMenu);
            block.addArgument("PIN", menuDigitalOutputPin);
            block.addArgument("ONOFF", menuOnOff);
        });

    /* blockCommand(1, "set_pwm_gpio", "Set pwm [VALUE] to pin [PIN]", this::writePwmPinCommand, block -> {
      block.addArgument("PIN", menuAnalogOutputPin);
      block.addArgument("VALUE", 255);
    });*/

        blockCommand(1, "set_analog_gpio", "Set analog [VALUE] to pin [PIN] of [RPI]", this::writeAnalogPinCommand, block -> {
            block.addArgument("RPI", this.rpiIdMenu);
            block.addArgument("PIN", menuAnalogOutputPin);
            block.addArgument("VALUE", 255);
        });

        blockReporter(2, "get_gpio", "[PIN] value of [RPI]", this::getGPIOStateReporter, block -> {
            block.addArgument("RPI", this.rpiIdMenu);
            block.addArgument("PIN", menuInputPin);
        });

        blockHat(3, "when_gpio", "when [PIN] is [ONOFF] of [RPI]", this::whenGpioInStateHat, block -> {
            block.addArgument("RPI", this.rpiIdMenu);
            block.addArgument("PIN", menuDigitalInputPin);
            block.addArgument("ONOFF", menuOnOff);
        });

        blockReporter(4, "DS18B20_status", "DS18B20 [DS18B20] of [RPI]", this::getDS18B20ValueHandler, block -> {
            block.addArgument("RPI", this.rpiIdMenu);
            block.addArgument("DS18B20", menuDS18B20);
        });
    }

    @Override
    public void init() {
        this.rpiIdMenu.setDefault(entityContext.findAny(GpioEntity.class));
    }

    private void writeAnalogPinCommand(WorkspaceBlock workspaceBlock) {
        int address = getAddress(workspaceBlock, menuAnalogOutputPin);
        GpioEntity entity = workspaceBlock.getMenuValueEntityRequired("RPI", this.rpiIdMenu);
        entity.getService().setValue(address, new DecimalType(workspaceBlock.getInputInteger("VALUE")));
    }

    private State getDS18B20ValueHandler(WorkspaceBlock workspaceBlock) {
        String ds18b20Id = workspaceBlock.getMenuValue("DS18B20", menuDS18B20);
        GpioEntity entity = workspaceBlock.getMenuValueEntityRequired("RPI", this.rpiIdMenu);
        return new DecimalType(entity.getService().getDS18B20Value(ds18b20Id));
    }

    private void whenGpioInStateHat(WorkspaceBlock workspaceBlock) {
        int address = getAddress(workspaceBlock, menuDigitalInputPin);
        workspaceBlock.handleNext(next -> {
            OnOffType expectedState = OnOffType.of(workspaceBlock.getMenuValue("ONOFF", this.menuOnOff) == OnOffTypeEnum.On);
            BroadcastLock lock = workspaceBlock.getBroadcastLockManager().getOrCreateLock(workspaceBlock);
            GpioEntity entity = workspaceBlock.getMenuValueEntityRequired("RPI", this.rpiIdMenu);
            GPIOService gpioService = entity.getService();

            gpioService.addGpioListener(workspaceBlock.getId(), address, state -> {
                if (state == expectedState) {
                    lock.signalAll();
                }
            });
            workspaceBlock.onRelease(() -> gpioService.removeGpioListener(address, workspaceBlock.getId()));
            workspaceBlock.subscribeToLock(lock, next::handle);
        });
    }

    private State getGPIOStateReporter(WorkspaceBlock workspaceBlock) {
        int address = getAddress(workspaceBlock, menuInputPin);
        GpioEntity entity = workspaceBlock.getMenuValueEntityRequired("RPI", this.rpiIdMenu);
        return entity.getService().getState(address);
    }

    private void writeDigitalPinCommand(WorkspaceBlock workspaceBlock) {
        OnOffType value = OnOffType.of(workspaceBlock.getMenuValue("ONOFF", this.menuOnOff) == OnOffTypeEnum.On);
        int address = getAddress(workspaceBlock, menuDigitalOutputPin);
        GpioEntity entity = workspaceBlock.getMenuValueEntityRequired("RPI", this.rpiIdMenu);
        entity.getService().setValue(address, value);
    }

    private int getAddress(WorkspaceBlock workspaceBlock, ServerMenuBlock menuPin) {
        return Integer.parseInt(workspaceBlock.getMenuValue("PIN", menuPin));
    }
}
