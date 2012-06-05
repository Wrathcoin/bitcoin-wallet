/*
 * Copyright 2011-2012 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.schildbach.wallet.Constants;

/**
 * @author Andreas Schildbach
 */
public class AutosyncReceiver extends BroadcastReceiver
{
	private static final long AUTOSYNC_INTERVAL = AlarmManager.INTERVAL_HOUR;

	private PendingIntent alarmIntent;

	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		final String action = intent.getAction();
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (Intent.ACTION_POWER_CONNECTED.equals(action))
		{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			final boolean autosync = prefs.getBoolean(Constants.PREFS_KEY_AUTOSYNC, false);

			if (autosync)
			{
				final Intent serviceIntent = new Intent(context, BlockchainServiceImpl.class);

				context.startService(serviceIntent);

				alarmIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
				am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AUTOSYNC_INTERVAL, alarmIntent);
			}
			else
			{
				cancelAlarm(am);
			}
		}
		else if (Intent.ACTION_POWER_DISCONNECTED.equals(action))
		{
			cancelAlarm(am);
		}
	}

	private void cancelAlarm(final AlarmManager am)
	{
		if (alarmIntent != null)
		{
			am.cancel(alarmIntent);
			alarmIntent = null;
		}
	}
}
