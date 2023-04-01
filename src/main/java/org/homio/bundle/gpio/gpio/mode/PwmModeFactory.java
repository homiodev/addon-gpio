package org.homio.bundle.gpio.gpio.mode;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.analog.AnalogInput;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;
import org.homio.bundle.gpio.gpio.GpioProviderIdModel;
import org.homio.bundle.gpio.gpio.GpioState;
import org.homio.bundle.api.exception.ProhibitedExecution;
import org.homio.bundle.api.state.DecimalType;
import org.homio.bundle.api.state.State;

public class PwmModeFactory implements GpioModeFactory<AnalogInput> {

    @Override
    public void createGpioState(Context pi4j, GpioState gpioState, GpioProviderIdModel gpioProvidersIdModel) {
        gpioState.setInstance(pi4j.create(Pwm.newConfigBuilder(pi4j)
                                             .name(gpioState.getGpioPin().getName())
                                             .address(gpioState.getGpioPin().getAddress())
                                             .frequency(1000)      // optionally pre-configure the desired frequency to 1KHz
                                             .dutyCycle(50)        // optionally pre-configure the desired duty-cycle (50%)
                                             .shutdown(0)  // optionally pre-configure a shutdown duty-cycle value (on terminate)
                                             //.initial(50)         // optionally pre-configure an initial duty-cycle value (on startup)
                                             .provider(gpioProvidersIdModel.getPwmProviderId())
                                             .pwmType(PwmType.HARDWARE)
                                             .initial(0)
                                             .shutdown(0)
                                             .build()));
    }

    @Override
    public State getState(AnalogInput instance) {
        return new DecimalType(instance.value());
    }

    @Override
    public void setState(AnalogInput instance, State state) {
        throw new ProhibitedExecution();
    }
}
