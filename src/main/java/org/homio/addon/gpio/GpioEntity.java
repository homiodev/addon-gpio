package org.homio.addon.gpio;

import jakarta.persistence.Entity;
import org.homio.api.Context;
import org.homio.api.entity.CreateSingleEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.entity.log.HasEntityLog;
import org.homio.api.entity.types.MicroControllerBaseEntity;
import org.homio.api.model.OptionModel;
import org.homio.api.model.device.ConfigDeviceDefinition;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.service.EntityService;
import org.homio.api.ui.UISidebarChildren;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldSlider;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.selection.dynamic.DynamicOptionLoader;
import org.homio.api.ui.field.selection.dynamic.UIFieldDynamicSelection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static org.homio.addon.gpio.GPIOService.GPIO_PROVIDERS;

@SuppressWarnings({"JpaAttributeTypeInspection", "unused"})
@Entity
@UISidebarChildren(icon = "fas fa-keyboard", color = "#92BA1A")
@CreateSingleEntity
public final class GpioEntity extends MicroControllerBaseEntity
  implements DeviceEndpointsBehaviourContract, EntityService<GPIOService>, HasEntityLog {

  @UIField(order = 4)
  @UIFieldDynamicSelection(SelectGpioProviders.class)
  public String getGpioProvider() {
    return getJsonData("prv", "MOCK");
  }

  public void setGpioProvider(String value) {
    setJsonData("prv", value);
  }

  @UIField(order = 4)
  @UIFieldSlider(min = 1, max = 120, step = 5, header = "S")
  public int getOneWireInterval() {
    return getJsonData("owi", 30);
  }

  public void setOneWireInterval(int value) {
    setJsonData("owi", value);
  }

  @Override
  public String getDefaultName() {
    return "Gpio";
  }

  @Override
  protected @NotNull String getDevicePrefix() {
    return "gpio";
  }

  @Override
  public void logBuilder(EntityLogBuilder entityLogBuilder) {
    entityLogBuilder.addTopic("org.homio.bundle.gpio");
  }

  @Override
  public @NotNull Class<GPIOService> getEntityServiceItemClass() {
    return GPIOService.class;
  }

  @Override
  public @NotNull GPIOService createService(@NotNull Context context) {
    return new GPIOService(context, this);
  }

  @Override
  public @NotNull String getDeviceFullName() {
    return getTitle();
  }

  @Override
  public @NotNull Map<String, ? extends DeviceEndpoint> getDeviceEndpoints() {
    return getService().getEndpoints();
  }

  @Override
  public @NotNull List<ConfigDeviceDefinition> findMatchDeviceConfigurations() {
    return List.of();
  }

  @Override
  public long getEntityServiceHashCode() {
    return 0;
  }

  @Override
  public void assembleActions(UIInputBuilder uiInputBuilder) {

  }

  public static class SelectGpioProviders implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
      return OptionModel.list(GPIO_PROVIDERS.keySet());
    }
  }
}
