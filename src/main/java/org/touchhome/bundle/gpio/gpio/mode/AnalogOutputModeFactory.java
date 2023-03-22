package org.touchhome.bundle.gpio.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.analog.AnalogOutput;
import java.util.function.Consumer;
import org.touchhome.bundle.api.exception.ProhibitedExecution;
import org.touchhome.bundle.api.state.DecimalType;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.gpio.gpio.GpioProviderIdModel;
import org.touchhome.bundle.gpio.gpio.GpioState;

public class AnalogOutputModeFactory implements GpioModeFactory<AnalogOutput> {

    @Override
    public void createGpioState(Context pi4j, GpioState gpioState, GpioProviderIdModel gpioProviderIdModel) {
        gpioState.setInstance(pi4j.create(AnalogOutput.newConfigBuilder(pi4j)
                                                      .name(gpioState.getGpioPin().getName())
                                                      .address(gpioState.getGpioPin().getAddress())
                                                      .provider(gpioProviderIdModel.getAnalogOutputProviderId())
                                                      .build())
                                  .addListener(event -> {
                                      DecimalType state = new DecimalType(event.value(), event.oldValue());
                                      if (state.equalToOldValue()) {
                                          gpioState.setLastState(state);
                                          for (Consumer<State> listener : gpioState.getListeners().values()) {
                                              listener.accept(state);
                                          }
                                      }
                                  }));
    }

    @Override
    public State getState(AnalogOutput instance) {
        throw new ProhibitedExecution();
    }

    @Override
    public void setState(AnalogOutput instance, State state) {
        instance.setValue(state.intValue());
    }
}
