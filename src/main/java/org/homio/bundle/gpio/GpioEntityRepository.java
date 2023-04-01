package org.homio.bundle.gpio;

import com.pi4j.io.gpio.digital.PullResistance;
import java.util.HashSet;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.homio.bundle.gpio.gpio.GpioPin;
import org.homio.bundle.gpio.gpio.GpioPinEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.repository.AbstractRepository;

@Log4j2
@Repository
public class GpioEntityRepository extends AbstractRepository<GpioEntity> {

    private final EntityContext entityContext;

    public GpioEntityRepository(EntityContext entityContext) {
        super(GpioEntity.class);
        this.entityContext = entityContext;
    }

    @Override
    @Transactional
    public GpioEntity save(GpioEntity entity) {
        GpioEntity raspberryDevice = super.save(entity);
        if (raspberryDevice.getGpioPinEntities() == null) {
            raspberryDevice.setGpioPinEntities(new HashSet<>());
            for (GpioPin gpioPin : RaspberryGpioPin.getGpioPins()) {
                GpioPinEntity pin = new GpioPinEntity();
                pin.setEntityID(gpioPin.getAddress() + "_" + raspberryDevice.getEntityID());
                pin.setPull(PullResistance.PULL_DOWN);
                pin.setColor(gpioPin.getColor());
                pin.setName(gpioPin.getName());
                pin.setDescription(gpioPin.getDescription());
                pin.setAddress(gpioPin.getAddress());
                pin.setPosition(gpioPin.getAddress());
                pin.setSupportedModes(gpioPin.getSupportModes().stream().map(Enum::name).collect(Collectors.joining("~~~")));
                pin.setOwner(raspberryDevice);
                raspberryDevice.getGpioPinEntities().add(entityContext.save(pin));
            }
        }
        return raspberryDevice;
    }
}
