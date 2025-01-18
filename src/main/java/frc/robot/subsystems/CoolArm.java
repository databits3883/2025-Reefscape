// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import java.util.function.Predicate;

import javax.annotation.processing.SupportedOptions;

import org.dyn4j.geometry.Rotation;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkAbsoluteEncoder;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkRelativeEncoder;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.proto.Wpimath;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.ExponentialProfile.ProfileTiming;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.TimeUnit;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.units.Units.*;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.sysid.SysIdRoutineLog;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Robot;
import frc.robot.RobotContainer;
import frc.robot.Constants.CoolArmConstants;
import edu.wpi.first.wpilibj.sysid.SysIdRoutineLog.State;;

public class CoolArm extends SubsystemBase {

  public static enum ArmAction{
    L1,
    L2,
    L3,
    L4,
    Travel,
    Pickup,
    Place
  }

  private SparkMax angleMotor = new SparkMax(CoolArmConstants.angleCANID, MotorType.kBrushless);
  public SparkAbsoluteEncoder absAngleEncoder = angleMotor.getAbsoluteEncoder();
  private ArmFeedforward armFFController = new ArmFeedforward(CoolArmConstants.kS, CoolArmConstants.kG, CoolArmConstants.kV);
  private PIDController armPIDController = new PIDController(CoolArmConstants.kP, CoolArmConstants.kI, CoolArmConstants.kD);
  private TrapezoidProfile.Constraints trapezoidConstraints = new TrapezoidProfile.Constraints((65d/0.75d), (65d/0.75d)/(0.75d*0.5d ));
  private TrapezoidProfile.State previousTrapezoidState = new TrapezoidProfile.State(0, 0);
  private TrapezoidProfile angleTrapezoidProfile = new TrapezoidProfile(trapezoidConstraints);
  private Timer trapezoidTimer = new Timer();
  private double angleSetpoint = 5;
  private double elevatorSetpoint = 0;
  private double elevatorTolerance = 0.25;


  public SparkMax elevatorMotor = new SparkMax(CoolArmConstants.elevatorCANID, MotorType.kBrushless);
  public RelativeEncoder elevatorEncoder = elevatorMotor.getEncoder();
  private boolean elevatorControlEnabled = false;

  // public SysIdRoutine sysIdRoutine = new SysIdRoutine(
  //   new SysIdRoutine.Config(Volts.of( 0.15 ).per(Units.Seconds), Volts.of(0.7), Seconds.of(10)),
  //   //new SysIdRoutine.Config(),
  //   new SysIdRoutine.Mechanism(this::voltageDrive, this::logMotors, this));

  /** Creates a new CoolArm. */
  public CoolArm() {
    // Creates a SysIdRoutine
    
      
    Shuffleboard.getTab("Arm Sysid Testing").addDouble("Absolute Angle", absAngleEncoder::getPosition);
    Shuffleboard.getTab("Arm Sysid Testing").addDouble("Angle ProfileGoal", () -> angleSetpoint);
    Shuffleboard.getTab("Arm Sysid Testing").addDouble("Angle Setpoint", () -> previousTrapezoidState.position);

    Shuffleboard.getTab("Arm Sysid Testing").addDouble("Angle Motor Current", angleMotor::getOutputCurrent);
    
    Shuffleboard.getTab("Arm Sysid Testing").addDouble("Angle Motor Output", angleMotor::getAppliedOutput);
    Shuffleboard.getTab("Arm Sysid Testing").addDouble("Elevator Position", elevatorEncoder::getPosition);
    armPIDController.setIZone(20);

    angleSetpoint = absAngleEncoder.getPosition();
    SetElevatorEncoderPosition(0);
    elevatorSetpoint = elevatorEncoder.getPosition();

    //Shuffleboard.getTab("Arm Sysid Testing").add(armPIDController);
    SmartDashboard.putData(armPIDController);
    
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    double absAngle = absAngleEncoder.getPosition();
    
    previousTrapezoidState = angleTrapezoidProfile.calculate(trapezoidTimer.get(), previousTrapezoidState, new TrapezoidProfile.State(angleSetpoint,0));
    trapezoidTimer.restart();

    //SetAngleMotor(armPIDController.calculate(absAngle,previousTrapezoidState.position ) + armFFController.calculate( ( (previousTrapezoidState.position - 90) / 180) * Math.PI, 0));

    if(elevatorControlEnabled){
      if(elevatorEncoder.getPosition() > elevatorSetpoint + elevatorTolerance){
        SetElevatorMotor(-1);
      }
      else if(elevatorEncoder.getPosition() < elevatorSetpoint - elevatorTolerance){
        SetElevatorMotor(1);
      }
      else{
        SetElevatorMotor(0);
      }
    }
    
    
  }

  

  public void SetArmAction(ArmAction newAction){
    double newAngleSP = absAngleEncoder.getPosition();
    double newElevatorSP = elevatorEncoder.getPosition();


    switch(newAction){
      case L1:
        newAngleSP = CoolArmConstants.kL1PrepAngleSP;
        newElevatorSP = CoolArmConstants.kL1PrepElevatorSP;

        break;
      case L2:
        newAngleSP = CoolArmConstants.kL2PrepAngleSP;
        newElevatorSP = CoolArmConstants.kL2PrepElevatorSP;
        break;
      case L3:
        newAngleSP = CoolArmConstants.kL3PrepAngleSP;
        newElevatorSP = CoolArmConstants.kL3PrepElevatorSP;
        break;
      case L4:
        newAngleSP = CoolArmConstants.kL4PrepAngleSP;
        newElevatorSP = CoolArmConstants.kL4PrepElevatorSP;
        break;
      case Travel:
        newAngleSP = CoolArmConstants.kTravelAngleSP;
        newElevatorSP = CoolArmConstants.kTravelElevatorSP;
        break;
      case Pickup:
        newAngleSP = CoolArmConstants.kPickupAngleSP;
        newElevatorSP = CoolArmConstants.kPickupElevatorSP;
        break;
      case Place:
        newAngleSP += CoolArmConstants.kPlaceAngleSPChange;
        newElevatorSP += CoolArmConstants.kPlaceElevatorSPChange;
        break;
    }

    SetAngleSetpoint(newAngleSP);
    SetElevatorSetpoint(newElevatorSP); 
  }

  public void SetAngleSetpoint(double sp){
    angleSetpoint = sp;
  }

  public void SetAngleMotor(double speed){
    angleMotor.setVoltage(speed);
  }

  public void SetElevatorSetpoint(double sp){
    elevatorSetpoint = sp;
    SetElevatorControlEnabled(true);
  }

  public void SetElevatorMotor(double voltage){
    elevatorMotor.setVoltage(voltage);
  }

  public void SetElevatorMotorManual(double voltage){
    SetElevatorControlEnabled(false);
    SetElevatorMotor(voltage);
  }

  public void SetElevatorEncoderPosition(double newValue){
    elevatorEncoder.setPosition(newValue);
  }

  public void SetElevatorControlEnabled(boolean enabled){
    elevatorControlEnabled = enabled;
    if(!enabled){
      SetElevatorMotor(0);
    }
  }

//   public void voltageDrive(Voltage volts){
//       angleMotor.setVoltage(volts);
//   }

//   public void logMotors(SysIdRoutineLog log){
//     log.motor("AngleMotor").voltage(Volts.of( angleMotor.getAppliedOutput()*angleMotor.getBusVoltage() ) ).angularPosition(Degrees.of(absAngleEncoder.getPosition())).angularVelocity(DegreesPerSecond.of(absAngleEncoder.getVelocity()));
// }
}
