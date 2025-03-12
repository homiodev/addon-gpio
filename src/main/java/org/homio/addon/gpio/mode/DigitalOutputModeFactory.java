package org.homio.addon.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import org.hibernate.service.spi.ServiceException;
import org.homio.addon.gpio.GpioPinEndpoint;
import org.homio.addon.gpio.GpioProviderIdModel;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;

import java.util.Objects;
import java.util.function.Consumer;

public class DigitalOutputModeFactory implements GpioModeFactory<DigitalOutput> {

  private static void handler(GpioPinEndpoint endpoint, DigitalStateChangeEvent event) {
    OnOffType state = OnOffType.of(event.state().isHigh());
    if (!Objects.equals(endpoint.getValue(), state)) {
      endpoint.setValueFromDevice(state);
      for (Consumer<State> listener : endpoint.getListeners().values()) {
        listener.accept(state);
      }
    }
  }

  @Override
  public void createGpioState(Context pi4j, GpioPinEndpoint endpoint, GpioProviderIdModel idModel) {
    DigitalOutputConfig config = DigitalOutput.newConfigBuilder(pi4j)
      .name(endpoint.getGpioPin().getName())
      .address(endpoint.getGpioPin().getAddress())
      .provider(idModel.digitalOutputProviderId())
      .build();
    endpoint.setInstance(pi4j.create(config)
      .addListener(event -> handler(endpoint, event)));
  }

  @Override
  public State getState(DigitalOutput instance) {
    throw new ServiceException("DigitalOutput does not support getting state");
  }

  @Override
  public void setState(DigitalOutput instance, State state) {
    instance.setState(state.boolValue());
  }
}
