package org.homio.addon.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.analog.AnalogInput;
import com.pi4j.io.gpio.analog.AnalogInputConfig;
import com.pi4j.io.gpio.analog.AnalogValueChangeEvent;
import org.hibernate.service.spi.ServiceException;
import org.homio.addon.gpio.GpioPinEndpoint;
import org.homio.addon.gpio.GpioProviderIdModel;
import org.homio.api.state.DecimalType;
import org.homio.api.state.State;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class AnalogInputModeFactory implements GpioModeFactory<AnalogInput> {

  private static void handler(GpioPinEndpoint endpoint, AnalogValueChangeEvent event) {
    DecimalType state = new DecimalType(new BigDecimal(event.value()), new BigDecimal(event.oldValue()));
    if (state.equalToOldValue()) {
      endpoint.setValueFromDevice(state);
      for (Consumer<State> listener : endpoint.getListeners().values()) {
        listener.accept(state);
      }
    }
  }

  @Override
  public void createGpioState(Context pi4j, GpioPinEndpoint endpoint, GpioProviderIdModel idModel) {
    AnalogInputConfig config = AnalogInput.newConfigBuilder(pi4j)
      .name(endpoint.getGpioPin().getName())
      .address(endpoint.getGpioPin().getAddress())
      .provider(idModel.analogInputProviderId())
      .build();
    endpoint.setInstance(pi4j.create(config)
      .addListener(event -> handler(endpoint, event)));
  }

  @Override
  public State getState(AnalogInput instance) {
    return new DecimalType(instance.value());
  }

  @Override
  public void setState(AnalogInput instance, State state) {
    throw new ServiceException("AnalogInput does not support setting state");
  }
}
