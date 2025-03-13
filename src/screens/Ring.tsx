import React, {useEffect, useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {getAlarm, snoozeAlarm, stopAlarm} from '../alarm';
import Button from '../components/Button';
import {colors, globalStyles} from '../global';
import {useNavigation, useRoute, RouteProp} from '@react-navigation/native';

// Define the type for the Alarm object
type Alarm = {
  title: string;
  getTimeString: () => {hour: string; minutes: string};
};

// Define the route params type
type RootStackParamList = {
  Ring: {alarmUid: string}; // The 'Ring' screen expects an 'alarmUid' as a string
};

export default function Ring() {
  const navigation = useNavigation();
  const route = useRoute<RouteProp<RootStackParamList, 'Ring'>>(); // Type the route with the 'Ring' screen params
  const [alarm, setAlarm] = useState<Alarm | null>(null); // Type the alarm state

  useEffect(() => {
    const alarmUid = route.params.alarmUid;
    (async function () {
      const fetchedAlarm = await getAlarm(alarmUid); // Assume getAlarm returns an Alarm object
      setAlarm(fetchedAlarm);
    })();
  }, [route.params.alarmUid]); // Dependency array ensures this effect runs when alarmUid changes

  if (!alarm) {
    return <View />; // Render nothing if alarm is null
  }

  return (
    <View style={globalStyles.container}>
      <View style={[globalStyles.innerContainer, styles.container]}>
        <View style={styles.textContainer}>
          <Text style={styles.clockText}>
            {alarm.getTimeString().hour} : {alarm.getTimeString().minutes}
          </Text>
          <Text style={styles.title}>{alarm.title}</Text>
        </View>
        <View style={styles.buttonContainer}>
          <Button
            title={'Snooze'}
            onPress={async () => {
              await snoozeAlarm();
              navigation.goBack();
            }}
          />
          <Button
            title={'Stop'}
            onPress={async () => {
              await stopAlarm();
              navigation.goBack();
            }}
          />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  clockText: {
    color: 'black',
    fontWeight: 'bold',
    fontSize: 50,
  },
  textContainer: {
    display: 'flex',
    alignItems: 'center',
  },
  buttonContainer: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
  },
  title: {
    fontWeight: 'bold',
    fontSize: 20,
    color: colors.GREY,
  },
});
