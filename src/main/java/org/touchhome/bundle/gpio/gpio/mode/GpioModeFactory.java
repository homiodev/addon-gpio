package org.touchhome.bundle.gpio.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.IO;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.gpio.gpio.GpioProviderIdModel;
import org.touchhome.bundle.gpio.gpio.GpioState;

public interface GpioModeFactory<T extends IO> {

    void createGpioState(Context pi4j, GpioState gpioState, GpioProviderIdModel gpioProvidersIdModel);

    State getState(T instance);

    void setState(T instance, State state);

    default void destroy(GpioState gpioState) {

    }
}
