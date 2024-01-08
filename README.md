# react-native-azure-speech-sdk

Package that provides text to speech and speech to text using azure speech sdk.

## Installation

```sh
npm install react-native-azure-speech-sdk
```

## Usage

```
//text to speech
export default App = () => {

    const [text, setText] = useState('')


    return (
        <View>
            <TextInput
                value={text}
                style={{ color: 'green' }}
                onChangeText={(e) => setText(e)}
            />
            <Button title="Test" onPress={() => textToSpeech('your_azure_subscription_key', 'your_region', 'your_language', text)} />
        </View>
    )
}

//speech to text
export default App = () => {

  const [text, setText] = useState('')


  const getResultss = async () => {
    speechToText('your_azure_subscription_key', 'your_region', 'your_language').then(result => {
      setText(result)
    })
  }

  return <SafeAreaView style={styles.container}>
    <Pressable style={styles.buttonStyles} onPress={
      () => {
        recordAudio();
      }}>
      <Text style={styles.textButton}>Micstream start</Text>
    </Pressable>
    <Pressable style={styles.buttonStyles} onPress={
      () => {
        getResultss();
      }}>
      <Text style={styles.textButton}>Micstream stop</Text>
    </Pressable>
    {text?.length > 0 && <Text style={styles.textStyle}>{text}</Text>}
  </SafeAreaView>
};

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: "center",
    alignItems: 'center',
    gap: 36,
    flexDirection: 'column',
  },
  textStyle: {
    color: 'green',
  },
  buttonStyles: {
    padding: 15,
    backgroundColor: "white",
    borderRadius: 15,
  },
  textButton: {
    color: "black",
  }
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

```

```
