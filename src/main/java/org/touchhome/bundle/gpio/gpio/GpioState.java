package org.touchhome.bundle.gpio.gpio;

import com.pi4j.io.IO;
import com.pi4j.io.gpio.digital.PullResistance;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.gpio.gpio.mode.PinMode;

@Getter
@RequiredArgsConstructor
public class GpioState {

    private final Map<String, Consumer<State>> listeners = new HashMap<>();
    private final @NotNull Logger log;
    private final @NotNull GpioPin gpioPin;
    private final @NotNull PinMode pinMode;
    private final @Nullable PullResistance pull;

    @Setter
    private IO instance;
    private State lastState;

    public void setLastState(State lastState) {
        log.debug("Update state: '{}' for pin: '{}'", lastState, gpioPin.getName());
        this.lastState = lastState;
    }

    @Override
    public String toString() {
        return "GpioState{pin=" + gpioPin + ", mode=" + pinMode + ", pull=" + pull + "}";
    }
}
