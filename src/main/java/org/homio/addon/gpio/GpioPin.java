package org.homio.addon.gpio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.homio.addon.gpio.mode.PinMode;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GpioPin implements Comparable<GpioPin> {

  private int address;
  private @NotNull String description;
  private @NotNull String name;
  private @Nullable Icon icon;
  private Set<PinMode> supportModes;

  @Override
  public int compareTo(@NotNull GpioPin o) {
    return this.address - o.address;
  }

  @Override
  public String toString() {
    return "[" + address + "/" + name + "']";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GpioPin gpioPin = (GpioPin) o;

    return address == gpioPin.address;
  }

  @Override
  public int hashCode() {
    return address;
  }
}
