package org.touchhome.bundle.gpio.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import java.util.Objects;
import java.util.function.Consumer;
import org.touchhome.bundle.api.exception.ProhibitedExecution;
import org.touchhome.bundle.api.state.OnOffType;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.gpio.gpio.GpioProviderIdModel;
import org.touchhome.bundle.gpio.gpio.GpioState;

public class DigitalInputModeFactory implements GpioModeFactory<DigitalInput> {

    @Override
    public void createGpioState(Context pi4j, GpioState gpioState, GpioProviderIdModel gpioProvidersIdModel) {
        gpioState.setInstance(pi4j.create(DigitalInput.newConfigBuilder(pi4j)
                                                      .name(gpioState.getGpioPin().getName())
                                                      .address(gpioState.getGpioPin().getAddress())
                                                      .pull(gpioState.getPull())
                                                      .provider(gpioProvidersIdModel.getDigitalInputProviderId())
                                                      .build())
                                  .addListener(event -> {
                                      OnOffType state = OnOffType.of(event.state().isHigh());
                                      if (!Objects.equals(gpioState.getLastState(), state)) {
                                          gpioState.setLastState(state);
                                          for (Consumer<State> listener : gpioState.getListeners().values()) {
                                              listener.accept(state);
                                          }
                                      }
                                  }));
    }

    @Override
    public State getState(DigitalInput instance) {
        return OnOffType.of(instance.state().isHigh());
    }

    @Override
    public void setState(DigitalInput instance, State state) {
        throw new ProhibitedExecution();
    }
}
