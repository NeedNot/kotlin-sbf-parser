# kotlin-sbf-parser
SBF (Septentrio binary format) parser for kotlin

Loosly based on [core-marine-dev/sbf-parser](https://github.com/core-marine-dev/sbf-parser)
I created this for my personal use there may be bugs or incomplete features, use at your own risk or contribute to make it better :)

## Usage
1. **Create an instance of `SBFParser`**
2. **Pass a `ByteArray` to `addData()`** - This will parse the data.
   - **Full successful blocks** can then be retrieved from `getBlocks()`.
   - **Invalid blocks** will be discarded.
   - **Incomplete blocks** will remain in the buffer for the next parsing.
3. **Check leftover data** with `.length`
4. **Clear data** with `.clearData()`

### Supported Blocks
Only some blocks are currently supported, which can be found in [`Blocks.kt`](https://github.com/NeedNot/kotlin-sbf-parser/blob/main/src/main/kotlin/net/neednot/sbfparser/Blocks.kt). If the parser encounters a block that is not recognized, it will treat it as an invalid block and ignore it.

## Contributions
I've only added the blocks I needed. If you want to add more or have suggestions, please open an issue or fork the repository and create a pull request. I would love to merge your contributions!

### Adding New Blocks
To add a new block:
- Create a new data class that extends `BlockBody`.
- Implement the `name` field with the name of the block.
- Create fields for every block field.
- Strings and arrays should use the `@ArraySize` annotation
- Sub-blocks should use the `@SubBlockList` annotation
- Add the block ID to the `blockClassById` map like this:
  ```kotlin
  100 to MyBlock::class.java
  ```

## Installation
Add it in your root build.gradle at the end of repositories
```
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

Add the dependency
```
dependencies {
  implementation 'com.github.NeedNot:kotlin-sbf-parser:Tag'
}
```
