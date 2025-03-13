import React, {useEffect, useState} from 'react';
import {StyleSheet, View} from 'react-native';
import Alarm, {removeAlarm, scheduleAlarm, updateAlarm} from '../alarm';
import TextInput from '../components/TextInput';
import DayPicker from '../components/DayPicker';
import TimePicker from '../components/TimePicker';
import Button from '../components/Button';
import {globalStyles} from '../global';
import SwitcherInput from '../components/SwitcherInput';
import {useNavigation, useRoute, RouteProp} from '@react-navigation/native';

type EditScreenParams = {
  alarm?: Partial<Alarm>;
};

type RouteParams = RouteProp<{params?: EditScreenParams}, 'params'>;

export default function Edit() {
  const navigation = useNavigation();
  const route = useRoute<RouteParams>();
  const [alarm, setAlarm] = useState<Alarm | null>(null);
  const [mode, setMode] = useState<'EDIT' | 'CREATE' | null>(null);

  useEffect(() => {
    if (route.params?.alarm) {
      setAlarm(new Alarm(route.params.alarm));
      setMode('EDIT');
    } else {
      setAlarm(new Alarm());
      setMode('CREATE');
    }
  }, [route.params]);

  function update(updates: [keyof Alarm, any][]) {
    if (!alarm) return;
    const updatedAlarm = new Alarm({
      ...alarm,
      ...updates.reduce((acc, [key, value]) => ({...acc, [key]: value}), {}),
    });
    setAlarm(updatedAlarm);
  }

  async function onSave() {
    if (!alarm) return;
    if (mode === 'EDIT') {
      alarm.active = true;
      await updateAlarm(alarm);
    } else if (mode === 'CREATE') {
      await scheduleAlarm(alarm);
    }
    navigation.goBack();
  }

  async function onDelete() {
    if (alarm) {
      await removeAlarm(alarm.uid);
      navigation.goBack();
    }
  }

  if (!alarm) {
    return <View />;
  }

  return (
    <View style={globalStyles.container}>
      <View style={[globalStyles.innerContainer, styles.container]}>
        <View style={styles.inputsContainer}>
          <TimePicker
            onChange={({h, m}: any) =>
              update([
                ['hour', h],
                ['minutes', m],
              ])
            }
            hour={alarm.hour}
            minutes={alarm.minutes}
          />
          <TextInput
            description={'Title'}
            style={styles.textInput}
            onChangeText={(v: string) => update([['title', v]])}
            value={alarm.title}
          />
          <TextInput
            description={'Description'}
            style={styles.textInput}
            onChangeText={(v: string) => update([['description', v]])}
            value={alarm.description}
          />
          <SwitcherInput
            description={'Repeat'}
            value={alarm.repeating}
            onChange={(v: string) => update([['repeating', v]])}
          />
          {alarm.repeating && (
            <DayPicker
              onChange={v => {
                update([['days', v]]);
                return null;
              }}
              activeDays={alarm.days}
            />
          )}
        </View>
        <View style={styles.buttonContainer}>
          {mode === 'EDIT' && <Button onPress={onDelete} title={'Delete'} />}
          <Button fill={true} onPress={onSave} title={'Save'} />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'space-around',
    alignItems: 'center',
    height: '100%',
  },
  inputsContainer: {
    width: '100%',
  },
  buttonContainer: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  textInput: {},
});
