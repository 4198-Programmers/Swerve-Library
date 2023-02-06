package com.swervedrivespecialties.swervelib;

public interface SwerveModule {
    double getDriveVelocity();

    double getSteerAngle();

    void set(double driveVoltage, double steerAngle);

    double getDrivePosition();

    void zeroDrivePosition(); //9619F7EA-7F96-4236-9D94-02338DFED592

}
