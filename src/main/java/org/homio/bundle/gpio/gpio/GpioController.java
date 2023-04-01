package org.homio.bundle.gpio.gpio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.entity.BaseEntity;
import org.homio.bundle.api.model.OptionModel;
import org.homio.bundle.gpio.GpioEntity;
import org.homio.bundle.gpio.gpio.mode.PinMode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/gpio")
public class GpioController {

    private final EntityContext entityContext;

    @GetMapping("/device/DS18B20")
    public List<BaseEntity> getRaspberryDS18B20() {
        List<BaseEntity> list = new ArrayList<>();
        List<GpioEntity> entities = entityContext.findAll(GpioEntity.class);
        for (GpioEntity entity : entities) {
            list.addAll(entity.getService().getDS18B20()
                              .stream().map(s -> BaseEntity.fakeEntity(s).setName(s)).collect(Collectors.toList()));
        }
        return list;
    }

    @GetMapping("/pin/{mode}")
    public List<OptionModel> getPins(@PathVariable("mode") Mode mode, @RequestParam("rpiIdMenu") String rpiIdMenu) {
        GpioEntity gpioEntity = entityContext.getEntityRequire(rpiIdMenu);
        Map<Integer, GpioState> stateMap = gpioEntity.getService().getState();

        return stateMap.values().stream()
                       .filter(mode::accept).map(gpioState ->
                OptionModel.of(String.valueOf(gpioState.getGpioPin().getAddress()),
                    fixNum(gpioState.getGpioPin().getAddress()) + "/" + gpioState.getGpioPin().getName())).collect(Collectors.toList());
    }

    private String fixNum(int address) {
        return address < 9 ? "0" + address : String.valueOf(address);
    }

    public enum Mode {
        input(PinMode.DIGITAL_INPUT, PinMode.ANALOG_INPUT),
        output(PinMode.DIGITAL_OUTPUT, PinMode.ANALOG_OUTPUT),
        digitalInput(PinMode.DIGITAL_INPUT),
        digitalOutput(PinMode.DIGITAL_OUTPUT),
        analogInput(PinMode.ANALOG_INPUT),
        analogOutput(PinMode.ANALOG_OUTPUT),
        pwm(PinMode.PWM);

        private final PinMode[] modes;

        Mode(PinMode... modes) {
            this.modes = modes;
        }

        public boolean accept(GpioState gpioState) {
            for (PinMode mode : modes) {
                if (gpioState.getPinMode() == mode) {
                    return true;
                }
            }
            return false;
        }
    }
}
