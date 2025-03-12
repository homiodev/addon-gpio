package org.homio.addon.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.analog.AnalogOutput;
import com.pi4j.io.gpio.analog.AnalogOutputConfig;
import com.pi4j.io.gpio.analog.AnalogValueChangeEvent;
import org.hibernate.service.spi.ServiceException;
import org.homio.addon.gpio.GpioPinEndpoint;
import org.homio.addon.gpio.GpioProviderIdModel;
import org.homio.api.state.DecimalType;
import org.homio.api.state.State;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class AnalogOutputModeFactory implements GpioModeFactory<AnalogOutput> {

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
    AnalogOutputConfig config = AnalogOutput.newConfigBuilder(pi4j)
      .name(endpoint.getGpioPin().getName())
      .address(endpoint.getGpioPin().getAddress())
      .provider(idModel.analogOutputProviderId())
      .build();
    endpoint.setInstance(pi4j.create(config)
      .addListener(event -> handler(endpoint, event)));
  }

  @Override
  public State getState(AnalogOutput instance) {
    throw new ServiceException("AnalogOutput does not support getting state");
  }

  @Override
  public void setState(AnalogOutput instance, State state) {
    instance.setValue(state.intValue());
  }
}
