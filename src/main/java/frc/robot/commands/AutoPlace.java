// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;


import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.ActiveDriveToPose.GoalType;
import frc.robot.subsystems.CoolArm;
import frc.robot.subsystems.SignalLights;
import frc.robot.subsystems.CoolArm.ArmAction;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class AutoPlace extends Command {

  private CoolArm coolArm;
  private SwerveSubsystem swerveSubsystem;
  private Timer backupDelayTimer = new Timer();
  private boolean algaeRemoval = false;
  private boolean toStationNonProcessor = false;

  private ChassisSpeeds towardsReefSpeeds = new ChassisSpeeds(0, 0, 0);

  /** Creates a new AutoPlace. */
  public AutoPlace(CoolArm arm, SignalLights lights, SwerveSubsystem swerve,boolean attemptAlgaeRemoval,boolean autoStationNonProcessor) {
    coolArm = arm;
    swerveSubsystem = swerve;
    algaeRemoval = attemptAlgaeRemoval;
    toStationNonProcessor =  autoStationNonProcessor;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(arm,swerve);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    coolArm.SetArmAction(ArmAction.Place);
    backupDelayTimer.restart();
    if(!toStationNonProcessor){
      driveReverse();
    }
    else{
      Pose2d stationPose = swerveSubsystem.getNonProcesserSideStation();
      Transform2d toStation = stationPose.minus(swerveSubsystem.getPose());
      Translation2d translationTowardsStation = new Translation2d(2,toStation.getTranslation().getAngle());
      towardsReefSpeeds = new ChassisSpeeds(translationTowardsStation.getX()-1, translationTowardsStation.getY(), 0);
      driveTowardStation();
    }
    
    
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if(!toStationNonProcessor){
      driveReverse();
    }
    else {
      driveTowardStation();
    }
    
    
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
     
    return !coolArm.HasCoralInGripper() || (algaeRemoval ? backupDelayTimer.hasElapsed(1.35) : backupDelayTimer.hasElapsed(1));
  }

  public void driveReverse(){
    if(algaeRemoval ? backupDelayTimer.hasElapsed(0.75) : backupDelayTimer.hasElapsed(0.25) ){
      if(algaeRemoval){
        swerveSubsystem.setChassisSpeeds(new ChassisSpeeds(-2,1,0.25));

      }
      else{
        swerveSubsystem.setChassisSpeeds(new ChassisSpeeds(-2,0,0));

      }

    }
    else{
      swerveSubsystem.setChassisSpeeds(new ChassisSpeeds(0,0,0));

    }
  }

  public void driveTowardStation(){
    if(algaeRemoval ? backupDelayTimer.hasElapsed(0.75) : backupDelayTimer.hasElapsed(0.25) ){
      
      swerveSubsystem.setChassisSpeeds(towardsReefSpeeds);

      

    }
    else{
      swerveSubsystem.setChassisSpeeds(new ChassisSpeeds(0,0,0));

    }
  }
}
