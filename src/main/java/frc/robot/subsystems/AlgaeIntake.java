// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkMax;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector.SingleArgConstructor;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.AlgaeIntakeConstants;
import frc.robot.subsystems.SignalLights.LightSignal;

public class AlgaeIntake extends SubsystemBase {

  public static enum IntakeState{
    Deployed,
    Deploying,
    Retracted,
    Retracting
  }

  private SparkFlex m_intakeMotor = new SparkFlex(AlgaeIntakeConstants.INTAKE_MOTOR_ID, MotorType.kBrushless);
  private SparkMax m_angleMotor = new SparkMax(AlgaeIntakeConstants.ANGLE_MOTOR_ID, MotorType.kBrushless);
  private RelativeEncoder m_angleEncoder = m_angleMotor.getEncoder();
  public IntakeState currentState = IntakeState.Retracted;
  public SignalLights signalLights;


  /** Creates a new AlgaeIntake. */
  public AlgaeIntake(SignalLights lights) {
    signalLights = lights;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    signalLights.ReceiveIntakeData(hasAlgae());

    switch (currentState) {
      case Deployed:
        SetAngleMotor(0);
        break;
      case Deploying:
        SetAngleMotor(0.01);
        if(m_angleEncoder.getPosition() > AlgaeIntakeConstants.INTAKE_DEPLOY_ANGLE){
          currentState = IntakeState.Deployed;
        }
        break;
      case Retracted:
        SetAngleMotor(0);
        break;
      case Retracting:
        SetAngleMotor(-0.01);
        if(m_angleEncoder.getPosition() < AlgaeIntakeConstants.INTAKE_DEPLOY_ANGLE){
          currentState = IntakeState.Retracted;
        }
        break;
      default:
        SetAngleMotor(0);
        break;

    }

    
  }

  public void SetAngleMotor(double voltage){
    m_angleMotor.setVoltage(voltage);
  }

  public void deployIntake(){
    currentState = IntakeState.Deploying;
    
  }

  public void retractIntake(){
    currentState = IntakeState.Retracting;
  }

  public void SetIntakeMotor(double voltage){
    m_intakeMotor.setVoltage(voltage);
  }

  public void StopIntake(){
    SetIntakeMotor(0);
    signalLights.SetSignal(LightSignal.databits);
  }

  public void Intake(){
    SetIntakeMotor(AlgaeIntakeConstants.kINTAKE_SPEED);
    signalLights.SetSignal(LightSignal.hasAlgae);
  }

  public void Outtake(){
    SetIntakeMotor(AlgaeIntakeConstants.kOUTTAKE_SPEED);
    signalLights.SetSignal(LightSignal.hasAlgae);
  }

  public boolean hasAlgae(){
    return false;
  }

  
}
