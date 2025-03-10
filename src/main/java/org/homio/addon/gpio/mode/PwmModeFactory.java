package org.homio.addon.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.analog.AnalogInput;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;
import org.hibernate.service.spi.ServiceException;
import org.homio.addon.gpio.GpioPinEndpoint;
import org.homio.addon.gpio.GpioProviderIdModel;
import org.homio.api.state.DecimalType;
import org.homio.api.state.State;

public class PwmModeFactory implements GpioModeFactory<AnalogInput> {

  @Override
  public void createGpioState(Context pi4j, GpioPinEndpoint endpoint, GpioProviderIdModel idModel) {
    endpoint.setInstance(pi4j.create(Pwm.newConfigBuilder(pi4j)
      .name(endpoint.getGpioPin().getName())
      .address(endpoint.getGpioPin().getAddress())
      .frequency(1000)      // optionally pre-configure the desired frequency to 1KHz
      .dutyCycle(50)        // optionally pre-configure the desired duty-cycle (50%)
      .shutdown(0)  // optionally pre-configure a shutdown duty-cycle value (on terminate)
      //.initial(50)         // optionally pre-configure an initial duty-cycle value (on startup)
      .provider(idModel.pwmProviderId())
      .pwmType(PwmType.HARDWARE)
      .initial(0)
      .shutdown(0)
      .build()));
  }

  @Override
  public State getState(AnalogInput instance) {
    return new DecimalType(instance.value());
  }

  @Override
  public void setState(AnalogInput instance, State state) {
    throw new ServiceException("Pwm does not support setting state");
  }
}
