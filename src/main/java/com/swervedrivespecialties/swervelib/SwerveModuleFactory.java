package com.swervedrivespecialties.swervelib;

import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;

public class SwerveModuleFactory<DriveConfiguration, SteerConfiguration> {
    private final ModuleConfiguration moduleConfiguration;
    private final DriveControllerFactory<?, DriveConfiguration> driveControllerFactory;
    private final SteerControllerFactory<?, SteerConfiguration> steerControllerFactory;

    public SwerveModuleFactory(ModuleConfiguration moduleConfiguration,
            DriveControllerFactory<?, DriveConfiguration> driveControllerFactory,
            SteerControllerFactory<?, SteerConfiguration> steerControllerFactory) {
        this.moduleConfiguration = moduleConfiguration;
        this.driveControllerFactory = driveControllerFactory;
        this.steerControllerFactory = steerControllerFactory;
    }

    public SwerveModule create(DriveConfiguration driveConfiguration, SteerConfiguration steerConfiguration) {
        var driveController = driveControllerFactory.create(driveConfiguration, moduleConfiguration);
        var steerController = steerControllerFactory.create(steerConfiguration, moduleConfiguration);

        return new ModuleImplementation(driveController, steerController);
    }

    public SwerveModule create(ShuffleboardLayout container, DriveConfiguration driveConfiguration,
            SteerConfiguration steerConfiguration) {
        var driveController = driveControllerFactory.create(
                container,
                driveConfiguration,
                moduleConfiguration);
        var steerContainer = steerControllerFactory.create(
                container,
                steerConfiguration,
                moduleConfiguration);

        return new ModuleImplementation(driveController, steerContainer);
    }

    private static class ModuleImplementation implements SwerveModule {
        private final DriveController driveController;
        private final SteerController steerController;

        private ModuleImplementation(DriveController driveController, SteerController steerController) {
            this.driveController = driveController;
            this.steerController = steerController;
        }

        @Override
        public double getDriveVelocity() {
            return driveController.getStateVelocity();
        }

        @Override
        public double getSteerAngle() {
            return steerController.getStateAngle();
        }

        @Override
        public double getDrivePosition() {
            return driveController.getStatePosition();
        }

        @Override
        public void zeroDrivePosition() {
            driveController.resetStateDrivePosition();
        }

        @Override
        public void set(double driveVoltage, double wantedAngleRad) {
            // wantedAngleRad %= (2.0 * Math.PI); //TODO changed
            // if (wantedAngleRad < 0.0) {
            // wantedAngleRad += 2.0 * Math.PI;
            // }

            double difference = wantedAngleRad - getSteerAngle();
            // Change the target angle so the difference is in the range [-pi, pi) instead
            // of [0, 2pi)
            // if (difference >= Math.PI) {
            // wantedAngleRad -= 2.0 * Math.PI;
            // } else if (difference < -Math.PI) {
            // wantedAngleRad += 2.0 * Math.PI;
            // }
            // difference = wantedAngleRad - getSteerAngle(); // Recalculate difference

            // If the difference is greater than 90 deg or less than -90 deg the drive can
            // be inverted so the total
            // movement of the module is less than 90 deg
            if (difference > Math.PI / 2.0 || difference < -Math.PI / 2.0) {
                // Only need to add 180 deg here because the target angle will be put back into
                // the range [0, 2pi)
                wantedAngleRad += Math.PI;
                driveVoltage *= -1.0;
            }

            // Put the target angle back into the range [0, 2pi)
            // wantedAngleRad %= (2.0 * Math.PI);
            if (wantedAngleRad < 0.0) {
                wantedAngleRad += 2.0 * Math.PI;
            } else if (wantedAngleRad > 2.0 * Math.PI) {
                wantedAngleRad -= 2.0 * Math.PI;
            }

            driveController.setReferenceVoltage(driveVoltage);
            steerController.setReferenceAngle(wantedAngleRad);
        }
    }
}
