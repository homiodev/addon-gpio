package org.homio.bundle.gpio;

import static org.homio.bundle.gpio.gpio.mode.PinMode.DIGITAL_INPUT;
import static org.homio.bundle.gpio.gpio.mode.PinMode.DIGITAL_OUTPUT;
import static org.homio.bundle.gpio.gpio.mode.PinMode.ONE_WIRE;
import static org.homio.bundle.gpio.gpio.mode.PinMode.PWM;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.bundle.gpio.gpio.GpioPin;
import org.homio.bundle.gpio.gpio.mode.PinMode;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
public enum RaspberryGpioPin {
    PIN3(3, " I2C1 SDA", "GPIO_02", "#D0BC7F", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN5(5, " I2C1 SCL", "GPIO_03", "#D0BC7F", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN7(7, "   GPCLK0", "GPIO_04", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT, ONE_WIRE),
    PIN8(8, "UART0 TXD", "GPIO_14", "#DBB3A7", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN10(10, "UART0 RXD", "GPIO_15", "#DBB3A7", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN11(11, "      FL1", "GPIO_17", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN12(12, "  PCM CLK", "GPIO_18", "#8CD1F8", DIGITAL_INPUT, PWM, DIGITAL_OUTPUT),
    PIN13(13, " SD0 DAT3", "GPIO_27", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN15(15, "  SD0 CLK", "GPIO_22", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN16(16, "  SD0 CMD", "GPIO_23", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN18(18, " SD0 DAT0", "GPIO_24", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN19(19, "SPI0 MOSI", "GPIO_10", "#F1C16D", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN21(21, "SPI0 MISO", "GPIO_09", "#F1C16D", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN22(22, " SD0 DAT1", "GPIO_25", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN23(23, "SPI0 SCLK", "GPIO_11", "#F1C16D", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN24(24, " SPI0 CE0", "GPIO_08", "#F1C16D", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN26(26, " SPI0 CE1", "GPIO_07", "#F1C16D", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN27(27, " I2C0 SDA", "GPIO_00", "#F595A3", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN28(28, " I2C0 SCL", "GPIO_01", "#F595A3", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN29(29, "   GPCLK1", "GPIO_05", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN31(31, "   GPCLK2", "GPIO_06", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN32(32, "     PWM0", "GPIO_12", "#8CD1F8", DIGITAL_INPUT, PWM, DIGITAL_OUTPUT),
    PIN33(33, "     PWM1", "GPIO_13", "#8CD1F8", DIGITAL_INPUT, PWM, DIGITAL_OUTPUT),
    PIN35(35, "   PCM FS", "GPIO_19", "#8CD1F8", DIGITAL_INPUT, PWM, DIGITAL_OUTPUT),
    PIN36(36, "      FL0", "GPIO_16", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN37(37, " SD0 DAT2", "GPIO_26", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN38(38, "  PCM DIN", "GPIO_20", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT),
    PIN40(40, " PCM DOUT", "GPIO_21", "#8CD1F8", DIGITAL_INPUT, DIGITAL_OUTPUT);

    private final GpioPin gpioPin;

    RaspberryGpioPin(int address, String description, String name, String color, PinMode... supportModes) {
        this.gpioPin = new GpioPin(address, description, name, color, new HashSet<>(Arrays.asList(supportModes)));
    }

    @JsonCreator
    public static RaspberryGpioPin fromValue(String value) {
        return Stream.of(RaspberryGpioPin.values()).filter(dp -> dp.gpioPin.getName().equals(value)).findFirst().orElse(null);
    }

    public static Set<GpioPin> getGpioPins() {
        return Stream.of(RaspberryGpioPin.values()).map(RaspberryGpioPin::getGpioPin).collect(Collectors.toSet());
    }

    public static RaspberryGpioPin getPin(int address) {
        for (RaspberryGpioPin pin : RaspberryGpioPin.values()) {
            if (pin.getGpioPin().getAddress() == address) {
                return pin;
            }
        }
        throw new IllegalArgumentException("Unable to find pin with address: " + address);
    }

 /* public static List<RaspberryGpioPin> values(PinMode pinMode, PullResistance PullResistance) {
    return Stream.of(RaspberryGpioPin.values())
        .filter(p ->
            p.getPin().getSupportedPinModes().contains(pinMode) &&
                (PullResistance == null ||
                    p.getPin().getSupportedPullResistance().contains(PullResistance)))
        .sorted(Comparator.comparingInt(o -> o.address))
        .collect(Collectors.toList());
  }*/


    @JsonValue
    public String toValue() {
        return name();
    }

    @Override
    public String toString() {
        return gpioPin.getName() + " (" + gpioPin.getAddress() + ")";
    }
}
