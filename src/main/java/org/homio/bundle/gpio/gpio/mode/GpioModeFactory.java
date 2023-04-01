package org.homio.bundle.gpio.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.IO;
import org.homio.bundle.gpio.gpio.GpioProviderIdModel;
import org.homio.bundle.gpio.gpio.GpioState;
import org.homio.bundle.api.state.State;

public interface GpioModeFactory<T extends IO> {

    void createGpioState(Context pi4j, GpioState gpioState, GpioProviderIdModel gpioProvidersIdModel);

    State getState(T instance);

    void setState(T instance, State state);

    default void destroy(GpioState gpioState) {

    }
}
