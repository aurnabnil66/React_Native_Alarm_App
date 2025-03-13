import React from 'react';
import {StyleSheet, Text, TouchableOpacity} from 'react-native';
import 'react-native-gesture-handler';
import {NavigationContainer, ParamListBase} from '@react-navigation/native';
import {
  createStackNavigator,
  StackNavigationOptions,
  StackScreenProps,
} from '@react-navigation/stack';

import Home from './src/screens/Alarms';
import Edit from './src/screens/Edit';
import Ring from './src/screens/Ring';

// Create a type for your navigation params
export type RootStackParamList = {
  Alarms: undefined;
  Edit: undefined;
  Ring: {alarmUid: string}; // Assume Ring screen takes a parameter 'alarmUid'
};

const Stack = createStackNavigator<RootStackParamList>();

export default function App() {
  return (
    <NavigationContainer navigationInChildEnabled={true}>
      <Stack.Navigator>
        <Stack.Screen
          name="Alarms"
          component={Home}
          options={({
            navigation,
          }: StackScreenProps<RootStackParamList, 'Alarms'>) => ({
            ...headerStyles,
            title: 'Alarms',
            headerRight: () => (
              <AddButton
                title={'+ '}
                onPress={() => navigation.navigate('Edit')}
              />
            ),
          })}
        />
        <Stack.Screen name="Edit" component={Edit} options={headerStyles} />
        <Stack.Screen
          name="Ring"
          component={Ring}
          options={{headerShown: false}}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

type AddButtonProps = {
  title: string;
  onPress: () => void;
};

function AddButton({title, onPress}: AddButtonProps) {
  return (
    <TouchableOpacity style={styles.button} onPress={onPress}>
      <Text style={styles.buttonText}>{title}</Text>
    </TouchableOpacity>
  );
}

export const headerStyles: StackNavigationOptions = {
  headerStyle: {
    elevation: 0,
  },
  headerTintColor: '#000',
  headerTitleStyle: {
    fontWeight: 'bold',
  },
};

const styles = StyleSheet.create({
  button: {
    backgroundColor: 'transparent',
    padding: 10,
  },
  buttonText: {
    color: 'black',
    fontWeight: 'bold',
    fontSize: 25,
  },
});
