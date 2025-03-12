package org.homio.addon.gpio;

import com.pi4j.Pi4J;
import com.pi4j.boardinfo.definition.BoardType;
import com.pi4j.context.impl.DefaultContext;
import com.pi4j.plugin.mock.Mock;
import com.pi4j.plugin.mock.platform.MockPlatform;
import com.pi4j.plugin.mock.provider.gpio.analog.MockAnalogInputProvider;
import com.pi4j.plugin.mock.provider.gpio.analog.MockAnalogOutputProvider;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProvider;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProvider;
import com.pi4j.plugin.mock.provider.i2c.MockI2CProvider;
import com.pi4j.plugin.mock.provider.pwm.MockPwmProvider;
import com.pi4j.plugin.mock.provider.serial.MockSerialProvider;
import com.pi4j.plugin.mock.provider.spi.MockSpiProvider;
import com.pi4j.plugin.pigpio.PiGpioPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.homio.api.Context;
import org.homio.api.model.Icon;
import org.homio.api.service.EntityService;
import org.homio.api.state.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
public class GPIOService extends EntityService.ServiceInstance<GpioEntity> {

  public static final Map<String, GpioProviderIdModel> GPIO_PROVIDERS = new HashMap<>();

  private final Map<String, MutablePair<Long, Float>> ds18B20Values = new HashMap<>();
  @Getter
  private final Map<String, GpioPinEndpoint> endpoints = new HashMap<>();
  private final com.pi4j.context.Context pi4j;
  @Value("${w1BaseDir:/sys/devices/w1_bus_master1}")
  private Path w1BaseDir;

  @SneakyThrows
  public GPIOService(Context context, GpioEntity entity) {
    super(context, entity, true, "GPIO");
    this.pi4j = createContext();
  }

  public @Nullable State getState(int address) {
    var endpoint = getEndpoints().get(String.valueOf(address));
    return endpoint == null ? null : endpoint.getMode().getGpioModeFactory().getState(endpoint.getInstance());
  }

  public void setValue(int address, State state) {
    var endpoint = getEndpoints().get(String.valueOf(address));
    endpoint.setValue(state, true);
  }

  public void addGpioListener(String name, int address, Consumer<State> listener) {
    var endpoint = getEndpoints().get(String.valueOf(address));
    endpoint.getListeners().put(name, listener);
  }

  public void removeGpioListener(int address, String name) {
    var endpoint = getEndpoints().get(String.valueOf(address));
    endpoint.getListeners().remove(name);
  }

  public Float getDS18B20Value(String sensorID) {
    MutablePair<Long, Float> pair = ds18B20Values.get(sensorID);
    if (pair != null) {
      if (System.currentTimeMillis() - pair.getKey() < entity.getOneWireInterval() * 1000L) {
        return pair.getValue();
      }
    } else {
      pair = MutablePair.of(-1L, -1F);
      ds18B20Values.put(sensorID, pair);
    }
    pair.setLeft(System.currentTimeMillis());

    List<String> rawDataAsLines = getRawDataAsLines(sensorID);
    float value = -1;
    if (rawDataAsLines != null) {
      String line = rawDataAsLines.get(1);
      value = Float.parseFloat(line.substring(line.indexOf("t=") + "t=".length())) / 1000;
    }
    pair.setValue(value);

    return pair.getValue();
  }

  @SneakyThrows
  public List<String> getDS18B20() {
    if (System.getProperty("spring.profiles.active").contains("dev")) {
      return Collections.singletonList("28-test000011");
    }
    if (SystemUtils.IS_OS_LINUX && Files.exists(w1BaseDir.resolve("w1_master_slaves"))) {
      return Files.readAllLines(w1BaseDir.resolve("w1_master_slaves")).stream()
        .filter(sensorID -> sensorID.startsWith("28-"))
        .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  /* public void addGpioListener(String name, Pin pin, Consumer<PinState> listener) {
     assertDigitalInputPin(pin, PinMode.DIGITAL_INPUT, "Unable to add pin listener for not input pin mode");
     setGpioPinMode(pin, PinMode.DIGITAL_INPUT, null);
     digitalListeners.get(pin).add(new PinListener(name, event -> listener.accept(event.getState())));
   }
 */
 /* private void assertDigitalInputPin(Pin pin, PinMode digitalInput, String s) {
    if (!pin.getSupportedPinModes().contains(digitalInput)) {
      throw new IllegalArgumentException(s);
    }
  }*/

  /*public void setPullResistance(int address, PullResistance pullResistance) {
    GpioPinEndpoint gpioState = state.get(address);
    if (gpioState.getPinMode() == PinMode.DIGITAL_INPUT && gpioState.getPull() != pullResistance) {

    }

    GpioConfig gpioConfig = getGpioConfig(gpioPin);
    pi4j.create(gpioConfig);
    getGpioConfig(gpioPin);
    getDigitalInput(pin, pullResistance);
  }*/

  /*private void setGpioPinMode(Pin pin, PinMode pinMode, PullResistance PullResistance) {
    if (available) {
      GpioPin input = getDigitalInput(pin, PullResistance);
      if (input.getMode() != pinMode) {
        input.setMode(pinMode);
      }
    }
  }*/

  /*  public void addTrigger(HasTriggersEntity hasTriggersEntity, TriggerBaseEntity triggerBaseEntity, GpioPinDigitalInput input, GpioTrigger trigger) {
        log.info("Activate trigger: " + getTriggerName(trigger) + " for input: " + getPINName(input.getPin().getName()) + ". Device owner: " +
        hasTriggersEntity.getEntityID());
        input.addTrigger(trigger);
        if (!activeTriggers.containsKey(hasTriggersEntity)) {
            activeTriggers.put(hasTriggersEntity, new ArrayList<>());
        }
        List<ActiveTrigger> triggers = activeTriggers.get(hasTriggersEntity);
        triggers.addEnum(new ActiveTrigger(hasTriggersEntity, triggerBaseEntity, trigger, input));
    }*/

    /*public SpiDevice spiOpen(SpiChannel channel, int speed, SpiMode mode) throws IOException {
        return SpiFactory.getInstance(channel, speed, mode);
    }*/

  /*public void delay(int howLong) {
    Gpio.delay(howLong);
  }

  public byte[] spiXfer(SpiDevice handle, byte txByte) throws IOException {
    return handle.write(txByte);
  }

  public int spiXfer(SpiDevice handle, byte[] txBytes, byte[] data) throws IOException {
    byte[] bytes = handle.write(txBytes);
    for (int i = 0; i < data.length; i++) {
      if (bytes.length < i) {
        break;
      }
      data[i] = bytes[i];
    }
    return bytes.length;
  }*/

  /*public Set<String> getIButtons() throws IOException {
    return Files.list(w1BaseDir)
        .map(path -> path.getFileName().toString())
        .filter(path -> path.startsWith("01-")).collect(Collectors.toSet());
  }*/

  @Override
  protected void initialize() {
    endpoints.clear();
    context.var().createGroup("gpio", "GPIO", builder ->
      builder.setLocked(true).setIcon(new Icon("fas fa-keyboard", "#92BA1A")));
    // ensure variable exists
    /*for (GpioPin gpioPin : RaspberryGpioPin.getGpioPins()) {
      String varId = "rpi_" + entity.getEntityID() + "_" + gpioPin.getAddress();
      context.var().createVariable(entity.getEntityID(), varId,
        gpioPin.getName(), ContextVar.VariableType.Bool, builder ->
          builder.setDescription(gpioPin.getDescription())
            .setIcon(gpioPin.getIcon()));
    }*/
    for (GpioPin pin : RaspberryGpioPin.getGpioPins()) {
      endpoints.put(String.valueOf(pin.getAddress()), new GpioPinEndpoint(pin, entity, this));
    }

    GpioUtil.printInfo(pi4j, log);
    for (GpioPinEndpoint endpoint : endpoints.values()) {
      createOrUpdateState(endpoint, false);
    }
  }

  @Override
  public void destroy(boolean forRestart, @Nullable Exception ex) {
  }

    /*
      public GpioPin getGpioPin(GpioPin gpioPin) {
        return GpioFactory.getInstance().getProvisionedPin(gpioPin.getPin());
      }
    */

  private com.pi4j.context.Context createContext() {
    GPIO_PROVIDERS.put("MOCK", new GpioProviderIdModel(
      Mock.DIGITAL_INPUT_PROVIDER_ID,
      Mock.DIGITAL_OUTPUT_PROVIDER_ID,
      Mock.PWM_PROVIDER_ID,
      Mock.ANALOG_INPUT_PROVIDER_ID,
      Mock.ANALOG_OUTPUT_PROVIDER_ID,
      Mock.SPI_PROVIDER_ID,
      Mock.SERIAL_PROVIDER_ID,
      Mock.I2C_PROVIDER_ID
    ));
    var pij4Context = Pi4J.newAutoContext();
    var boardType = pij4Context.boardInfo().getBoardModel().getBoardType();
    if (boardType != BoardType.UNKNOWN) {
      GPIO_PROVIDERS.put("RPI", new GpioProviderIdModel(
        PiGpioPlugin.DIGITAL_INPUT_PROVIDER_ID,
        PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID,
        PiGpioPlugin.PWM_PROVIDER_ID,
        null,
        null,
        PiGpioPlugin.SPI_PROVIDER_ID,
        PiGpioPlugin.SERIAL_PROVIDER_ID,
        PiGpioPlugin.I2C_PROVIDER_ID
      ));
      return pij4Context;
    } else {
      return Pi4J.newContextBuilder()
        .add(new MockPlatform())
        .add(MockAnalogInputProvider.newInstance(),
          MockAnalogOutputProvider.newInstance(),
          MockSpiProvider.newInstance(),
          MockPwmProvider.newInstance(),
          MockSerialProvider.newInstance(),
          MockI2CProvider.newInstance(),
          MockDigitalInputProvider.newInstance(),
          MockDigitalOutputProvider.newInstance())
        .build();
    }
  }

  private List<String> getRawDataAsLines(String sensorID) {
    if (System.getProperty("spring.profiles.active").contains("dev")) {
      Random r = new Random(System.currentTimeMillis());
      return Arrays.asList("", "sd sd sd sd ff zz cc vv aa t=" + (10000 + r.nextInt(40000)));
    }

    Path path = w1BaseDir.resolve(sensorID).resolve("w1_slave");
    try {
      return FileUtils.readLines(path.toFile(), Charset.defaultCharset());
    } catch (IOException e) {
      log.error("Error while get RawData for sensor with id: " + sensorID);
      return null;
    }
  }

  public synchronized void createOrUpdateState(@NotNull GpioPinEndpoint endpoint, boolean update) {
    var gpioPin = endpoint.getGpioPin();
    var mode = endpoint.getMode();
    if (update && endpoint.getInstance() != null) {
      log.debug("Shutdown pin: <{}>", endpoint.getGpioPin().getName());
      endpoint.getInstance().shutdown(pi4j);
      DefaultContext defaultContext = (DefaultContext) pi4j;
      defaultContext.shutdown(endpoint.getInstance().id());
    }
    var model = GPIO_PROVIDERS.get(entity.getGpioProvider());
    mode.getGpioModeFactory().createGpioState(pi4j, endpoint, model);
    log.info("Created gpio interface: {}", endpoint);
    endpoint.getListeners().clear();
    // add global listener to link to variable
    endpoint.getListeners().put("rpi_global", state ->
      context.var().set("rpi_" + entity.getEntityID() + "_" + gpioPin.getAddress(), state));
  }
}
