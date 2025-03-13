import {Text, View} from 'react-native';
import {getAlarmState, getAllAlarms, disableAlarm, enableAlarm} from '../alarm';
import AlarmView from '../components/AlarmView';
import React, {useEffect, useState} from 'react';
import {globalStyles} from '../global';
import {useNavigation} from '@react-navigation/native';
import {AlarmProps} from '../alarm'; // Import the AlarmProps type

export default function Home() {
  const navigation = useNavigation();

  // Type the state variables properly
  const [alarms, setAlarms] = useState<AlarmProps[]>([]); // AlarmProps[] type for alarms
  const [scheduler, setScheduler] = useState<NodeJS.Timeout | null>(null); // Ensure scheduler is typed properly

  useEffect(() => {
    // Fetch all alarms when the screen is focused
    const fetchAlarms = async () => {
      const fetchedAlarms = await getAllAlarms();
      setAlarms(fetchedAlarms);
    };

    navigation.addListener('focus', async () => {
      await fetchAlarms();
      const intervalId = setInterval(fetchState, 10000);
      setScheduler(intervalId);
    });

    navigation.addListener('blur', () => {
      if (scheduler) clearInterval(scheduler); // Clear the interval when the screen loses focus
    });

    fetchState(); // Fetch the state on initial load
  }, [scheduler, navigation]);

  async function fetchState() {
    const alarmUid = await getAlarmState();
    if (alarmUid) {
      navigation.navigate('Ring', {alarmUid});
    }
  }

  return (
    <View style={globalStyles.container}>
      <View style={globalStyles.innerContainer}>
        {alarms.length === 0 && <Text>No alarms</Text>}
        {alarms.map(a => (
          <AlarmView
            key={a.uid}
            uid={a.uid}
            onChange={async (active: boolean) => {
              if (active) {
                await enableAlarm(a.uid?.toString() ?? '');
              } else {
                await disableAlarm(a.uid?.toString() ?? '');
              }
            }}
            onPress={() => navigation.navigate('Edit', {alarm: a})}
            title={a.title}
            hour={a.hour}
            minutes={a.minutes}
            days={a.days}
            isActive={a.active}
          />
        ))}
      </View>
    </View>
  );
}
