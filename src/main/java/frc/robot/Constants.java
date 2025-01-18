// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import swervelib.math.Matter;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean constants. This
 * class should not be used for any other purpose. All constants should be declared globally (i.e. public static). Do
 * not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants
{

  public static final double ROBOT_MASS = (148 - 20.3) * 0.453592; // 32lbs * kg per pound
  public static final Matter CHASSIS    = new Matter(new Translation3d(0, 0, Units.inchesToMeters(8)), ROBOT_MASS);
  public static final double LOOP_TIME  = 0.13; //s, 20ms + 110ms sprk max velocity lag
  public static final double MAX_SPEED  = Units.feetToMeters(14.5);
  // Maximum speed of the robot in meters per second, used to limit acceleration.

//  public static final class AutonConstants
//  {
//
//    public static final PIDConstants TRANSLATION_PID = new PIDConstants(0.7, 0, 0);
//    public static final PIDConstants ANGLE_PID       = new PIDConstants(0.4, 0, 0.01);
//  }

  public static final class DrivebaseConstants
  {

    // Hold time on motor brakes when disabled
    public static final double WHEEL_LOCK_TIME = 10; // seconds
  }

  public static class OperatorConstants
  {

    // Joystick Deadband
    public static final double DEADBAND        = 0.05;
    public static final double LEFT_Y_DEADBAND = 0.1;
    public static final double RIGHT_X_DEADBAND = 0.1;
    public static final double TURN_CONSTANT    = 6;
  }

  public static class CoolArmConstants
  {

    public static final int angleCANID = 14;
    public static final int elevatorCANID = 15;

    //Gains for the Arm angle controllers, both FF and PID
    public static final double kS = 0;
    public static final double kG = 2.07;//0.085 gains for no coral and set()
    public static final double kV = 0;
    public static final double kP = 0.03;//0.0025 gains for no coral and set()
    public static final double kI = 0.001;//0.001 gains for no coral and set()
    public static final double kD = 0;


    public static final double kL1PrepAngleSP = 57;
    public static final double kL2PrepAngleSP = 112;
    public static final double kL3PrepAngleSP = 139;
    public static final double kL4PrepAngleSP = 139;

    public static final double kTravelAngleSP = 6;
    public static final double kPickupAngleSP = 6;
    public static final double kPlaceAngleSPChange = -15;

    public static final double kL1PrepElevatorSP = 2.05;
    public static final double kL2PrepElevatorSP = 0.25;
    public static final double kL3PrepElevatorSP = 1.94;
    public static final double kL4PrepElevatorSP = 5.9;
    public static final double kTravelElevatorSP = 1.0;
    public static final double kPickupElevatorSP = 0;
    public static final double kPlaceElevatorSPChange = 0;

    
  }
}
