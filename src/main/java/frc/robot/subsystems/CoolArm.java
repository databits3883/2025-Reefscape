// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkAbsoluteEncoder;
import com.revrobotics.spark.SparkLimitSwitch;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import frc.robot.Constants.CoolArmConstants;;

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
  private ArmFeedforward armFFController = new ArmFeedforward(CoolArmConstants.kSAngle, CoolArmConstants.kGAngle, CoolArmConstants.kVAngle);
  private PIDController armPIDController = new PIDController(CoolArmConstants.kPAngle, CoolArmConstants.kIAngle, CoolArmConstants.kDAngle);
  private TrapezoidProfile.Constraints trapezoidConstraints = new TrapezoidProfile.Constraints((65d/0.25d), (65d/0.25d)/(0.25d*0.5d ));
  private TrapezoidProfile.State previousTrapezoidState = new TrapezoidProfile.State(0, 0);
  private TrapezoidProfile angleTrapezoidProfile = new TrapezoidProfile(trapezoidConstraints);
  private Timer trapezoidTimer = new Timer();
  private double angleSetpoint = 5;
  private double elevatorSetpoint = 0;


  private SparkMax elevatorMotor = new SparkMax(CoolArmConstants.elevatorCANID, MotorType.kBrushless);
  public RelativeEncoder elevatorEncoder = elevatorMotor.getEncoder();
  private PIDController elevatorPIDController = new PIDController(CoolArmConstants.kPElevator, CoolArmConstants.kIElevator, CoolArmConstants.kDElevator);
  private boolean elevatorControlEnabled = false;
  private SparkLimitSwitch raiseLimitSwitch = elevatorMotor.getReverseLimitSwitch();
  private SparkLimitSwitch lowerLimitSwitch = elevatorMotor.getForwardLimitSwitch();

  private ArmAction currentAction = ArmAction.L1;

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
    angleMotor.getEncoder().setPosition( -1d *  (( absAngleEncoder.getPosition()-90d ) / 360d ) * 42d);


    //Shuffleboard.getTab("Arm Sysid Testing").add(armPIDController);
    SmartDashboard.putData(armPIDController);
    SmartDashboard.putData(elevatorPIDController);
    
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    double absAngle = absAngleEncoder.getPosition();
    
    previousTrapezoidState = angleTrapezoidProfile.calculate(trapezoidTimer.get(), previousTrapezoidState, new TrapezoidProfile.State(angleSetpoint,0));
    trapezoidTimer.restart();

    double anglePIDOutput = GetAnglePIDOutput(absAngle);

    if(elevatorEncoder.getPosition() > CoolArmConstants.kTravelElevatorSP/2 && absAngle < CoolArmConstants.kMaxPickupBoxAngle){//if the elevator is above the travel position
      if(anglePIDOutput > 0){
        anglePIDOutput = 0;
      }
      
      //SetAngleMotor(armPIDController.calculate(absAngle,previousTrapezoidState.position ) + armFFController.calculate( ( (previousTrapezoidState.position - 180) / 180) * Math.PI, 0));
    }
    
    SetAngleMotor(anglePIDOutput);

    if(elevatorControlEnabled){
      //have to reverse this because the setvoltage is reversed and we have to invert this because the PID is smart enough to figure out which way to go
      SetElevatorMotor(-1 * Math.min(elevatorPIDController.calculate(elevatorEncoder.getPosition(), elevatorSetpoint),3));
    
    }

    if(raiseLimitSwitch.isPressed()){
      elevatorEncoder.setPosition(CoolArmConstants.kMaxElevatorPos);
    }
    else if (lowerLimitSwitch.isPressed()){
      elevatorEncoder.setPosition(0);
      if(currentAction == ArmAction.Pickup){
        SetElevatorControlEnabled(true);
        
      }
      //System.out.println("Made it to the lower limit");
    }
    
    
  }

  public double GetAnglePIDOutput(double angle){
    return armPIDController.calculate(angle,previousTrapezoidState.position ) + armFFController.calculate( ( (previousTrapezoidState.position - 180) / 180) * Math.PI, 0);

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
        newElevatorSP = CoolArmConstants.kTravelElevatorSP;
        
        break;
      case Place:

        //newAngleSP += CoolArmConstants.kPlaceAngleSPChange;
        //newElevatorSP += CoolArmConstants.kPlaceElevatorSPChange;
        newAngleSP = CoolArmConstants.kPlaceAngleSP;
        newElevatorSP = elevatorEncoder.getPosition();
        break;
    }

    SetAngleSetpoint(newAngleSP);
    SetElevatorSetpoint(newElevatorSP); 
    if(newAction == ArmAction.Pickup){
      SetElevatorMotorManual(-3);
    }

    currentAction = newAction;
  }

  public void SetAngleSetpoint(double sp){

    angleSetpoint = Math.max(90, Math.min(sp, 270));
  }

  public void SetAngleMotor(double speed){
    angleMotor.setVoltage(1 * speed);
  }

  public void SetElevatorSetpoint(double sp){
    elevatorSetpoint = sp;
    SetElevatorControlEnabled(true);
  }

  public void SetElevatorMotor(double voltage){
    //have to drive the motor negative to go up
    elevatorMotor.setVoltage(-1 * voltage);
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

  public void ManualAngleControl(CommandJoystick joystick) {
    SetAngleSetpoint(((joystick.getRawAxis(0) +1) * 90) + 90);
  }

//   public void voltageDrive(Voltage volts){
//       angleMotor.setVoltage(volts);
//   }

//   public void logMotors(SysIdRoutineLog log){
//     log.motor("AngleMotor").voltage(Volts.of( angleMotor.getAppliedOutput()*angleMotor.getBusVoltage() ) ).angularPosition(Degrees.of(absAngleEncoder.getPosition())).angularVelocity(DegreesPerSecond.of(absAngleEncoder.getVelocity()));
// }
}
