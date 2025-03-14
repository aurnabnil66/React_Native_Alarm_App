import {StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import React, {useState} from 'react';
import {colors} from '../global';

interface DayPickerProps {
  activeDays?: number[];
  onChange?: (days: number[]) => void;
}

interface DayProps {
  isActive: boolean;
  dayIndex: number;
  onUpdate: (dayIndex: number) => void;
}

export default function DayPicker({
  activeDays = [],
  onChange = (days: number[]) => null,
}: DayPickerProps) {
  // NOTICE: days doesn't change if prop activeDays changes
  const [days, setDays] = useState<number[]>(activeDays);

  function onDayChange(dayIndex: number) {
    let selectedBtn = getSelected(days);
    selectedBtn[dayIndex] = !selectedBtn[dayIndex];
    const newDays = getDays(selectedBtn);
    setDays(newDays);
    onChange(newDays);
  }

  return (
    <View style={styles.container}>
      <View style={styles.innerContainer}>
        {getSelected(days).map((isSelected, index) => (
          <Day
            key={index}
            isActive={isSelected}
            dayIndex={index}
            onUpdate={onDayChange}
          />
        ))}
      </View>
    </View>
  );
}

function Day({isActive, dayIndex, onUpdate}: DayProps) {
  return (
    <View style={{flex: 1}}>
      <TouchableOpacity
        style={[
          isActive ? styles.selectedBtn : styles.unselectedBtn,
          styles.btnContainer,
        ]}
        onPress={() => onUpdate(dayIndex)}>
        <Text
          style={[
            styles.text,
            isActive ? styles.selectedText : styles.unselectedText,
          ]}>
          {getDay(dayIndex)}
        </Text>
      </TouchableOpacity>
    </View>
  );
}

export function getSelected(activeDays: number[]): boolean[] {
  let selectedBtn = new Array(7).fill(false);
  for (let i = 0; i < activeDays.length; i++) {
    selectedBtn[activeDays[i]] = true;
  }
  return selectedBtn;
}

export function getDays(selectedBtn: boolean[]): number[] {
  let activeDays: number[] = [];
  for (let i = 0; i < selectedBtn.length; i++) {
    if (selectedBtn[i]) activeDays.push(i);
  }
  return activeDays;
}

function getDay(number: number): string {
  let weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  return weekdays[number];
}

const styles = StyleSheet.create({
  container: {
    display: 'flex',
    alignItems: 'flex-start',
    marginTop: 5,
    marginBottom: 5,
  },
  innerContainer: {
    display: 'flex',
    flexDirection: 'row',
  },
  btnContainer: {
    width: 40,
    height: 40,
    borderRadius: 20,
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  },
  selectedBtn: {
    color: 'black',
    fontWeight: 'bold',
    backgroundColor: colors.BLUE,
  },
  unselectedBtn: {
    color: 'black',
    borderWidth: 1,
    borderColor: colors.GREY,
  },
  text: {
    fontWeight: 'bold',
  },
  selectedText: {
    color: 'white',
  },
  unselectedText: {
    color: 'black',
  },
});
