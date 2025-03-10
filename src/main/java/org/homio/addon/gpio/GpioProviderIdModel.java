package org.homio.addon.gpio;

public record GpioProviderIdModel(
  String digitalInputProviderId,
  String digitalOutputProviderId,
  String pwmProviderId,
  String analogInputProviderId,
  String analogOutputProviderId,
  String spiProviderId,
  String serialProviderId,
  String i2cProviderId) {

}
