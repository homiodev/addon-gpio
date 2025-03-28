package org.homio.addon.gpio.mode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PinMode {
  DIGITAL_INPUT(new DigitalInputModeFactory()),
  DIGITAL_OUTPUT(new DigitalOutputModeFactory()),
  PWM(new PwmModeFactory()),
  ANALOG_INPUT(new AnalogInputModeFactory()),
  ANALOG_OUTPUT(new AnalogOutputModeFactory()),
  ONE_WIRE(null);

  private final GpioModeFactory gpioModeFactory;
}
