package org.homio.addon.gpio;

import lombok.RequiredArgsConstructor;
import org.homio.addon.gpio.mode.PinMode;
import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/gpio")
public class GpioController {

  private final Context context;

   /* @GetMapping("/device/DS18B20")
    public List<BaseEntity> getRaspberryDS18B20() {
        List<BaseEntity> list = new ArrayList<>();
        List<GpioEntity> entities = context.db().findAll(GpioEntity.class);
        for (GpioEntity entity : entities) {
            list.addAll(entity.getService().getDS18B20()
                              .stream().map(s -> BaseEntity.fakeEntity(s).setName(s)).toList());
        }
        return list;
    }*/

  @GetMapping("/pin/{mode}")
  public List<OptionModel> getPins(@PathVariable("mode") Mode mode, @RequestParam("rpiIdMenu") String rpiIdMenu) {
    GpioEntity gpioEntity = context.db().getRequire(rpiIdMenu);
    var endpoints = gpioEntity.getService().getEndpoints().values();

    return endpoints.stream()
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

    public boolean accept(GpioPinEndpoint gpioState) {
      for (PinMode mode : modes) {
        if (gpioState.getMode() == mode) {
          return true;
        }
      }
      return false;
    }
  }
}
