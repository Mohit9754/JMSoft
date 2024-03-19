/*
 * *
 *  * Created by Yash Jain on 29/03/23, 6:57 PM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 29/03/23, 6:57 PM
 *  * All rights reserved.
 *
 */
package com.jmsoft.Utility.UtilityTools;

import android.location.Location;

public interface LocationHelperCallback {
    void locationFetched(Location location);

    void locationPermissionDenied();

    void locationFetchFailure();
}

