package org.homio.bundle.gpio;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.bundle.gpio.gpio.GpioPin;
import org.springframework.stereotype.Component;
import org.homio.bundle.api.BundleEntrypoint;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.EntityContextVar.VariableType;
import org.homio.bundle.api.util.BoardInfo;

@Log4j2
@Component
@RequiredArgsConstructor
public class GpioEntrypoint implements BundleEntrypoint {

    private final EntityContext entityContext;

    public void init() {
        for (GpioEntity entity : entityContext.findAll(GpioEntity.class)) {
            createVariableGroup(entity);
        }
        entityContext.event().addEntityCreateListener(GpioEntity.class, "rpi-gen-create", this::createVariableGroup);
        entityContext.event().addEntityRemovedListener(GpioEntity.class, "rpi-gen-drop", entity -> {
            entityContext.var().removeGroup(entity.getEntityID());
        });
    }

    @Override
    public int order() {
        return 300;
    }

    @Override
    public BundleImageColorIndex getBundleImageColorIndex() {
        return BundleImageColorIndex.ONE;
    }

    public static String readBoardType() {

        // determine the board info by deciphering the revision number
        long irevision = Long.parseLong(BoardInfo.revision, 16);
        long scheme = (irevision >> 23) & 0x1;
        @SuppressWarnings("unused") long ram = (irevision >> 20) & 0x7;
        @SuppressWarnings("unused") long manufacturer = (irevision >> 16) & 0xF;
        @SuppressWarnings("unused") long processor = (irevision >> 12) & 0xF;
        long model = (irevision >> 4) & 0xFF;
        long pcbrev = irevision & 0xF;

        // determine board type based on revision scheme
        if (scheme > 0) {
            // a new revision scheme was provided with the release of Raspberry Pi 2
            // if the scheme bit is enabled, then use the new revision numbering scheme
            switch ((int) model) {
                case 0:
                    return "RaspberryPi_A";
                case 2:
                    return "RaspberryPi_A_Plus";
                case 3:
                    return "RaspberryPi_B_Plus";
                case 4:
                    return "RaspberryPi_2B";
                case 5:
                    return "RaspberryPi_Alpha";
                case 6:
                    return "RaspberryPi_ComputeModule";
                case 7:
                    return "RaspberryPi_Unknown";
                case 8:
                    return "RaspberryPi_3B";
                case 9:
                    return "RaspberryPi_Zero";
                case 10:
                    return "RaspberryPi_ComputeModule3";
                case 12:
                    return "RaspberryPi_ZeroW";
                case 13:
                    return "RaspberryPi_3B_Plus";
                case 14:
                    return "RaspberryPi_3A_Plus";
                case 16:
                    return "RaspberryPi_ComputeModule3_Plus";
                case 17:
                    return "RaspberryPi_4B";
                case 19:
                    return "RaspberryPi_400";
                case 20:
                    return "RaspberryPi_ComputeModule4";
                case 1:
                    return pcbrev <= 1 ? "RaspberryPi_B_Rev1" : "RaspberryPi_B_Rev2";
            }
        }

        // prior to the Raspberry Pi 2, the original revision scheme
        // was simply a fixed identifier number
        else {

            // The following info obtained from:
            // http://elinux.org/RPi_HardwareHistory
            // -and-
            // https://github.com/Pi4J/wiringPi/blob/master/wiringPi/wiringPi.c#L808

            // -------------------------------------------------------------------
            // Revision	Release Date	Model	PCB Revision	Memory	Notes
            // -------------------------------------------------------------------
            // Beta	  Q1 2012	B (Beta) ?.?	256 MB	Beta Board
            // 0002	  Q1 2012	B        1.0	256 MB
            // 0003	  Q3 2012	B     	 1.0	256 MB	(ECN0001) Fuses mod and D14 removed
            // 0004	  Q3 2012	B        2.0	256 MB	(Mfg by Sony)
            // 0005	  Q4 2012	B        2.0	256 MB	(Mfg by Qisda)
            // 0006	  Q4 2012	B        2.0	256 MB	(Mfg by Egoman)
            // 0007	  Q1 2013	A        2.0	256 MB	(Mfg by Egoman)
            // 0008	  Q1 2013	A        2.0	256 MB	(Mfg by Sony)
            // 0009	  Q1 2013	A        2.0	256 MB	(Mfg by Qisda)
            // 000d	  Q4 2012	B        2.0	512 MB	(Mfg by Egoman)
            // 000e	  Q4 2012	B        2.0	512 MB	(Mfg by Sony)
            // 000f	  Q4 2012	B        2.0	512 MB	(Mfg by Qisda)
            // 0010	  Q3 2014	B+       1.0	512 MB	(Mfg by Sony)
            // 0011	  Q2 2014	CM	     1.0	512 MB	(Mfg by Sony)
            // 0012	  Q4 2014	A+	     1.0	256 MB	(Mfg by Sony)
            // 0013	  Q1 2015	B+	     1.2	512 MB	 ?
            // 0014   ?? ????   CM       1.0    512 MB	(Mfg by Sony)
            // 0015   ?? ????   A+       1.1    256 MB 	(Mfg by Sony)|
            switch (BoardInfo.revision.trim()) {
                case "Beta":  // Model B Beta
                case "0002":  // Model B Revision 1
                case "0003":  // Model B Revision 1 (Egoman) + Fuses mod and D14 removed
                    return "RaspberryPi_B_Rev1";

                case "0004":  // Model B Revision 2 256MB (Sony)
                case "0005":  // Model B Revision 2 256MB (Qisda)
                case "0006":  // Model B Revision 2 256MB (Egoman)
                    return "RaspberryPi_B_Rev2";

                case "0007":  // Model A 256MB (Egoman)
                case "0008":  // Model A 256MB (Sony)
                case "0009":  // Model A 256MB (Qisda)
                    return "RaspberryPi_A";

                case "000d":  // Model B Revision 2 512MB (Egoman)
                case "000e":  // Model B Revision 2 512MB (Sony)
                case "000f":  // Model B Revision 2 512MB (Egoman)
                    return "RaspberryPi_B_Rev2";

                case "0010":  // Model B Plus 512MB (Sony)
                    return "RaspberryPi_B_Plus";

                case "0011":  // Compute Module 512MB (Sony)
                    return "RaspberryPi_ComputeModule";

                case "0012":  // Model A Plus 512MB (Sony)
                    return "RaspberryPi_A_Plus";

                case "0013":  // Model B Plus 512MB (Egoman)
                    return "RaspberryPi_B_Plus";

                /* UNDOCUMENTED */
                case "0014":  // Compute Module Rev 1.2, 512MB, (Sony)
                    return "RaspberryPi_ComputeModule";

                /* UNDOCUMENTED */
                case "0015":  // Model A Plus 256MB (Sony)
                    return "RaspberryPi_A_Plus";

                // unknown
                default:
                    return "RaspberryPi_Unknown";
            }
        }
        return "UNKNOWN";
    }

    private void createVariableGroup(GpioEntity entity) {
        entityContext.var().createGroup(entity.getEntityID(), "Raspberry[" + entity.getTitle() + "]", true, "fab fa-raspberry-pi", "#C70039");
        // ensure variable exists
        for (GpioPin gpioPin : RaspberryGpioPin.getGpioPins()) {
            entityContext.var().createVariable(entity.getEntityID(), "rpi_" + entity.getEntityID() + "_" + gpioPin.getAddress(),
                gpioPin.getName(), VariableType.Bool, builder -> builder.setDescription(gpioPin.getDescription()).setColor(gpioPin.getColor()));
        }
    }
}
