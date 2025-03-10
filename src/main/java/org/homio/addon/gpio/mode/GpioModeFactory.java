package org.homio.addon.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.IO;
import org.homio.addon.gpio.GpioPinEndpoint;
import org.homio.addon.gpio.GpioProviderIdModel;
import org.homio.api.state.State;

public interface GpioModeFactory<T extends IO> {

  void createGpioState(Context pi4j, GpioPinEndpoint endpoint, GpioProviderIdModel idModel);

  State getState(T instance);

  void setState(T instance, State state);

  default void destroy(GpioPinEndpoint gpioState) {

  }
}
