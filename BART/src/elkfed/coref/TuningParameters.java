/*
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

package elkfed.coref;

import elkfed.main.xml.TuningParameter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author yannick.versley
 */
public class TuningParameters {
    public static enum ParamType {
        Float,
        Integer,
        Boolean;
    }
    private final Map<String,Object> _params=new HashMap<String,Object>();
    public void readParameters(elkfed.main.xml.TuningParameters xmlParams)
    {
        for (TuningParameter param:xmlParams.getParameterArray()) {
            String name=param.getName();
            ParamType type=ParamType.valueOf(param.getType());
            Object value=null;
            if (type==ParamType.Float) {
                value=Float.parseFloat(param.getValue());
            } else if (type==ParamType.Integer) {
                value=Integer.parseInt(param.getValue());
            } else if (type==ParamType.Boolean) {
                value=Boolean.parseBoolean(param.getValue());
            }
            _params.put(name, value);
        }
    }

    public void updateParameters(elkfed.main.xml.TuningParameters xmlParams) {
        for (TuningParameter param: xmlParams.getParameterArray()) {
            param.setValue(_params.get(param.getName()).toString());
        }
    }

    public double getFloat(String key, double def_val)  {
        Float result=(Float)_params.get(key);
        if (result==null) return def_val;
        return result;
    }

    public int getInt(String key, int def_val) {
        Integer result=(Integer)_params.get(key);
        if (result==null) return def_val;
        return result;
    }

    public boolean getBool(String key, boolean def_val) {
        Boolean result=(Boolean)_params.get(key);
        if (result==null) return def_val;
        return result;
    }
}
