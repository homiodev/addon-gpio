package org.homio.addon.gpio;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.api.AddonConfiguration;
import org.homio.api.AddonEntrypoint;
import org.homio.api.Context;
import org.homio.api.ContextVar;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@AddonConfiguration
@RequiredArgsConstructor
public class GpioEntrypoint implements AddonEntrypoint {

  private final Context context;

  public void init() {
    for (GpioEntity entity : context.db().findAll(GpioEntity.class)) {
      createVariableGroup(entity);
    }
    context.event().addEntityCreateListener(GpioEntity.class, "rpi-gen-create", this::createVariableGroup);
    context.event().addEntityRemovedListener(GpioEntity.class, "rpi-gen-drop", entity -> {
      context.var().removeGroup(entity.getEntityID());
    });
  }

  @Override
  public @NotNull AddonImageColorIndex getAddonImageColorIndex() {
    return AddonImageColorIndex.ONE;
  }

  private void createVariableGroup(GpioEntity entity) {
    context.var().createGroup(entity.getEntityID(), "Raspberry[" + entity.getTitle() + "]",
      builder -> builder.setIcon(new Icon("fab fa-raspberry-pi", "#C70039")));
    // ensure variable exists
    for (GpioPin gpioPin : RaspberryGpioPin.getGpioPins()) {
      String varId = "rpi_" + entity.getEntityID() + "_" + gpioPin.getAddress();
      context.var().createVariable(entity.getEntityID(), varId,
        gpioPin.getName(), ContextVar.VariableType.Bool, builder ->
          builder.setDescription(gpioPin.getDescription())
            .setIcon(gpioPin.getIcon()));
    }
  }
}
