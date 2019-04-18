/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sb.firefds.pie.firefdskit.bean;

public class FeatureDTO {

    private String featureCode;
    private String featureValue;

    public FeatureDTO(String featureCode, String featureValue) {
        this.featureCode = featureCode;
        this.featureValue = featureValue;
    }

    public String getfeatureCode() {
        return featureCode;
    }

    public void setFeatureName(String featureCode) {
        this.featureCode = featureCode;
    }

    public String getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue(String featureValue) {
        this.featureValue = featureValue;
    }

}
