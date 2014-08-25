/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package pt.lighthouselabs.obd.reader.net;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.PUT;

/**
 * Definition of REST service available in OBD Server.
 */
public interface ObdService {

  @PUT("/")
  Response uploadReading(@Body String reading);

}