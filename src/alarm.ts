import {NativeModules} from 'react-native';
import {v4 as uuidv4} from 'uuid';

// Define the AlarmService interface
interface AlarmServiceInterface {
  set(alarm: AlarmProps): Promise<void>;
  enable(uid: string): Promise<void>;
  disable(uid: string): Promise<void>;
  stop(): Promise<void>;
  snooze(): Promise<void>;
  remove(uid: string): Promise<void>;
  update(alarm: AlarmProps): Promise<void>;
  removeAll(): Promise<void>;
  getAll(): Promise<AlarmProps[]>;
  get(uid: string): Promise<AlarmProps>;
  getState(): Promise<string | null>;
}

const AlarmService = NativeModules.AlarmModule as AlarmServiceInterface;

// Define an interface for the Alarm object
export interface AlarmProps {
  uid?: string;
  enabled?: boolean;
  title?: string;
  description?: string;
  hour?: number;
  minutes?: number;
  snoozeInterval?: number;
  repeating?: boolean;
  active?: boolean;
  days?: number[];
}

export default class Alarm {
  uid: string;
  enabled: boolean;
  title: string;
  description: string;
  hour: number;
  minutes: number;
  snoozeInterval: number;
  repeating: boolean;
  active: boolean;
  days: number[];

  constructor(params: AlarmProps = {}) {
    this.uid = params.uid ?? uuidv4();
    this.enabled = params.enabled ?? true;
    this.title = params.title ?? 'Alarm';
    this.description = params.description ?? 'Wake up';
    this.hour = params.hour ?? new Date().getHours();
    this.minutes = params.minutes ?? new Date().getMinutes() + 1;
    this.snoozeInterval = params.snoozeInterval ?? 1;
    this.repeating = params.repeating ?? false;
    this.active = params.active ?? true;
    this.days = params.days ?? [new Date().getDay()];
  }

  static getEmpty(): Alarm {
    return new Alarm({
      title: '',
      description: '',
      hour: 0,
      minutes: 0,
      repeating: false,
      days: [],
    });
  }

  toAndroid(): AlarmProps {
    return {
      ...this,
      days: toAndroidDays(this.days),
    };
  }

  static fromAndroid(alarm: AlarmProps): Alarm {
    return new Alarm({
      ...alarm,
      days: fromAndroidDays(alarm.days ?? []),
    });
  }

  getTimeString(): {hour: string; minutes: string} {
    return {
      hour: this.hour < 10 ? `0${this.hour}` : `${this.hour}`,
      minutes: this.minutes < 10 ? `0${this.minutes}` : `${this.minutes}`,
    };
  }

  getTime(): Date {
    const timeDate = new Date();
    timeDate.setHours(this.hour);
    timeDate.setMinutes(this.minutes);
    return timeDate;
  }
}

// Utility function for safely getting parameters
function getParam<T>(param: any, key: keyof AlarmProps, defaultValue: T): T {
  return param && param[key] !== undefined ? param[key] : defaultValue;
}

// Converts days for Android format
export function toAndroidDays(daysArray: number[]): number[] {
  return daysArray.map(day => (day + 1) % 7);
}

// Converts days from Android format
export function fromAndroidDays(daysArray: number[]): number[] {
  return daysArray.map(d => (d === 0 ? 6 : d - 1));
}

// Alarm Service Functions

export async function scheduleAlarm(alarm: AlarmProps): Promise<void> {
  const alarmInstance = alarm instanceof Alarm ? alarm : new Alarm(alarm);
  await AlarmService.set(alarmInstance.toAndroid());
  console.log('Scheduling alarm:', JSON.stringify(alarmInstance));
}

export async function enableAlarm(uid: string): Promise<void> {
  await AlarmService.enable(uid);
}

export async function disableAlarm(uid: string): Promise<void> {
  await AlarmService.disable(uid);
}

export async function stopAlarm(): Promise<void> {
  await AlarmService.stop();
}

export async function snoozeAlarm(): Promise<void> {
  await AlarmService.snooze();
}

export async function removeAlarm(uid: string): Promise<void> {
  await AlarmService.remove(uid);
}

export async function updateAlarm(alarm: AlarmProps): Promise<void> {
  const alarmInstance = alarm instanceof Alarm ? alarm : new Alarm(alarm);
  await AlarmService.update(alarmInstance.toAndroid());
}

export async function removeAllAlarms(): Promise<void> {
  await AlarmService.removeAll();
}

export async function getAllAlarms(): Promise<Alarm[]> {
  const alarms = await AlarmService.getAll();
  return alarms.map(a => Alarm.fromAndroid(a));
}

export async function getAlarm(uid: string): Promise<Alarm> {
  const alarm = await AlarmService.get(uid);
  return Alarm.fromAndroid(alarm);
}

export async function getAlarmState(): Promise<string | null> {
  return await AlarmService.getState();
}
