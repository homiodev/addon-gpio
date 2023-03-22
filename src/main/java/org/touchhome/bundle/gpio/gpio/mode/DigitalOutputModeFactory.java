package org.touchhome.bundle.gpio.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import java.util.Objects;
import java.util.function.Consumer;
import org.touchhome.bundle.api.exception.ProhibitedExecution;
import org.touchhome.bundle.api.state.OnOffType;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.gpio.gpio.GpioProviderIdModel;
import org.touchhome.bundle.gpio.gpio.GpioState;

public class DigitalOutputModeFactory implements GpioModeFactory<DigitalOutput> {

    @Override
    public void createGpioState(Context pi4j, GpioState gpioState, GpioProviderIdModel gpioProviderIdModel) {
        gpioState.setInstance(pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                                                       .name(gpioState.getGpioPin().getName())
                                                       .address(gpioState.getGpioPin().getAddress())
                                                       .provider(gpioProviderIdModel.getDigitalOutputProviderId())
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
    public State getState(DigitalOutput instance) {
        throw new ProhibitedExecution();
    }

    @Override
    public void setState(DigitalOutput instance, State state) {
        instance.setState(state.boolValue());
    }
}
