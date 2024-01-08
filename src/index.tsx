import {
  DeviceEventEmitter,
  PermissionsAndroid,
  Platform,
  NativeModules,
} from 'react-native';
// import 'react-native-get-random-values';
import 'node-libs-react-native/globals';
import { LogBox } from 'react-native';
import AudioRecord from 'react-native-audio-record';
import RNFS from 'react-native-fs';
import Sound from 'react-native-sound';

const LINKING_ERROR =
  `The package 'arber-azure-tts-test' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ArberAzureTtsTest = NativeModules.ArberAzureTtsTest
  ? NativeModules.ArberAzureTtsTest
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

LogBox.ignoreLogs(['new NativeEventEmitter']); // Ignore log notification by message

//prompt for permissions if not granted
const checkPermissions = async () => {
  if (Platform.OS === 'android') {
    try {
      const grants = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE!,
        PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE!,
        PermissionsAndroid.PERMISSIONS.RECORD_AUDIO!,
      ]);

      console.log('write external storage', grants);

      if (
        grants['android.permission.WRITE_EXTERNAL_STORAGE'] ===
          PermissionsAndroid.RESULTS.GRANTED &&
        grants['android.permission.READ_EXTERNAL_STORAGE'] ===
          PermissionsAndroid.RESULTS.GRANTED &&
        grants['android.permission.RECORD_AUDIO'] ===
          PermissionsAndroid.RESULTS.GRANTED
      ) {
        console.log('Permissions granted');
      } else {
        console.log('All required permissions not granted');
        return;
      }
    } catch (err) {
      console.warn(err);
      return;
    }
  }
};

//speech to text
export const recordAudio = async () => {
  try {
    await checkPermissions();

    const options = {
      sampleRate: 16000,
      channels: 1,
      bitsPerSample: 16,
      audioSource: 6,
      wavFile: 'work.wav',
    };

    AudioRecord.init(options);
    AudioRecord.start();
  } catch (error) {
    console.log(error);
  }
};

export const speechToText = async (
  subscriptionKey: string,
  region: string,
  language: string
  // filePath: string
) => {
  const filePath = await AudioRecord.stop();

  ArberAzureTtsTest.speechToText(subscriptionKey, region, language, filePath);

  const testPromise = new Promise((resolve) => {
    DeviceEventEmitter.addListener('NonVoidMethodResult', (result) => {
      const tempVal = JSON.parse(result);
      const textToBeReturned = tempVal.DisplayText;
      const processedText = textToBeReturned.replaceAll('.', '');
      resolve(processedText);
    });
  });

  return testPromise;
};

// text to speech

const handleToBase64 = async (data: string) => {
  const tempFilePath = RNFS.DocumentDirectoryPath + '/tempAudioFile.wav';

  await RNFS.writeFile(tempFilePath, data, 'base64');

  const sound = new Sound(tempFilePath, null!, (error) => {
    if (error) {
      console.error('Error loading sound:', error);
    } else {
      sound.play(() => {
        sound.release();

        RNFS.unlink(tempFilePath)
          .then(() => console.log('Temporary file deleted'))
          .catch((unlinkError) =>
            console.error('Error deleting temporary file:', unlinkError)
          );
      });
    }
  });
};

export const textToSpeech = async (
  subscriptionKey: string,
  region: string,
  language: string,
  text: string
) => {
  await checkPermissions();

  ArberAzureTtsTest.textToSpeech(subscriptionKey, region, language, text);

  DeviceEventEmitter.addListener('Test', (result) => {
    handleToBase64(result);
  });
};
