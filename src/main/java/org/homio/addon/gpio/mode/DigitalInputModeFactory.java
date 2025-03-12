package org.homio.addon.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import org.hibernate.service.spi.ServiceException;
import org.homio.addon.gpio.GpioPinEndpoint;
import org.homio.addon.gpio.GpioProviderIdModel;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;

import java.util.Objects;
import java.util.function.Consumer;

public class DigitalInputModeFactory implements GpioModeFactory<DigitalInput> {

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
    DigitalInputConfig config = DigitalInput.newConfigBuilder(pi4j)
      .name(endpoint.getGpioPin().getName())
      .address(endpoint.getGpioPin().getAddress())
      .pull(endpoint.getPull())
      .provider(idModel.digitalInputProviderId())
      .build();
    endpoint.setInstance(pi4j.create(config)
      .addListener(event -> handler(endpoint, event)));
  }

  @Override
  public State getState(DigitalInput instance) {
    return OnOffType.of(instance.state().isHigh());
  }

  @Override
  public void setState(DigitalInput instance, State state) {
    throw new ServiceException("DigitalInput does not support setting state");
  }
}
