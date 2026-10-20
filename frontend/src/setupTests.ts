/// <reference types="node" />

import '@testing-library/jest-dom';
import { TextEncoder, TextDecoder } from 'node:util';

if (!globalThis.TextEncoder) {
  globalThis.TextEncoder = TextEncoder as typeof globalThis.TextEncoder;
}

if (!globalThis.TextDecoder) {
  globalThis.TextDecoder = TextDecoder as typeof globalThis.TextDecoder;
}
