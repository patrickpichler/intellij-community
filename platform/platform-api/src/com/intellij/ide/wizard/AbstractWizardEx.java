/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.ide.wizard;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractWizardEx extends AbstractWizard<AbstractWizardStepEx> {

  private final String myTitle;
  private final Map<Object, Integer> myStepId2Index = new HashMap<Object, Integer>();
  private final Map<Integer, AbstractWizardStepEx> myIndex2Step = new HashMap<Integer, AbstractWizardStepEx>();

  public AbstractWizardEx(String title, @Nullable Project project, List<? extends AbstractWizardStepEx> steps) {
    super(title, project);
    myTitle = title;

    int index = 0;
    for (AbstractWizardStepEx step : steps) {
      myStepId2Index.put(step.getStepId(), index);
      myIndex2Step.put(index, step);
      addStep(step);

      step.addStepListener(new AbstractWizardStepEx.Listener() {
        public void stateChanged() {
          updateButtons();
        }

        public void doNextAction() {
          if (getNextButton().isEnabled()) {
            AbstractWizardEx.this.doNextAction();
          }
        }
      });
      index++;
    }

    init();
  }

  protected void doPreviousAction() {
    // Commit data of current step
    final AbstractWizardStepEx currentStep = mySteps.get(myCurrentStep);
    try {
      currentStep._commitPrev();
    }
    catch (final CommitStepCancelledException e) {
      return;
    }
    catch (CommitStepException e) {
      Messages.showErrorDialog(getContentPane(), e.getMessage());
      return;
    }

    myCurrentStep = getPreviousStep(myCurrentStep);
    updateStep();
  }

  protected void doNextAction() {
    // Commit data of current step
    final AbstractWizardStepEx currentStep = mySteps.get(myCurrentStep);
    try {
      currentStep._commit(false);
    }
    catch (final CommitStepCancelledException e) {
      return;
    }
    catch (CommitStepException e) {
      Messages.showErrorDialog(getContentPane(), e.getMessage());
      return;
    }

    myCurrentStep = getNextStep(myCurrentStep);
    updateStep();
  }

  protected int getNextStep(final int step) {
    AbstractWizardStepEx stepObject = myIndex2Step.get(step);
    Object nextStepId = stepObject.getNextStepId();
    return myStepId2Index.get(nextStepId);
  }

  protected int getPreviousStep(final int step) {
    AbstractWizardStepEx stepObject = myIndex2Step.get(step);
    Object previousStepId = stepObject.getPreviousStepId();
    return myStepId2Index.get(previousStepId);
  }

  protected String getHelpID() {
    return null;
  }

  protected void updateStep() {
    super.updateStep();
    updateButtons();
    setTitle(myTitle + ": " + getCurrentStepObject().getTitle());
  }

  private void updateButtons() {
    getNextButton().setEnabled(getCurrentStepObject().isComplete() && getCurrentStepObject().getNextStepId() != null);
    getPreviousButton().setEnabled(getCurrentStepObject().getPreviousStepId() != null);
    getFinishButton().setEnabled(canFinish());
  }

  protected boolean canFinish() {
    for (AbstractWizardStepEx step : mySteps) {
      if (!step.isComplete()) {
        return false;
      }
    }
    return true;
  }
}
