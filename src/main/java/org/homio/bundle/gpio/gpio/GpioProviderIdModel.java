package org.homio.bundle.gpio.gpio;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GpioProviderIdModel {

    private final String digitalInputProviderId;
    private final String digitalOutputProviderId;
    private final String pwmProviderId;
    private final String analogInputProviderId;
    private final String analogOutputProviderId;
    private final String spiProviderId;
    private final String serialProviderId;
    private final String i2cProviderId;
}
