from pathlib import Path
import json, hashlib
from jsonschema import Draft202012Validator
root=Path(__file__).resolve().parents[1]
skill=root/'skills/faceless-shorts'
load=lambda p: json.loads(p.read_text(encoding='utf-8'))
schema=load(root/'shared/schemas/skill-manifest.schema.json')
Draft202012Validator.check_schema(schema)
manifest=load(skill/'manifest.json')
Draft202012Validator(schema).validate(manifest)
inputs=load(skill/'input.schema.json')
Draft202012Validator.check_schema(inputs)
Draft202012Validator(inputs).validate(load(skill/'example-input.json'))
for field in ['entrypoint','input_schema','workflow','dependencies','permission_file','checksums']:
    assert (skill/manifest[field]).is_file(), field
for style in manifest['styles']:
    assert (skill/style['instructions']).is_file()
    assert (skill/style['example']).is_dir()
assert len({s['id'] for s in manifest['styles']}) == 6
assert set(manifest['permissions'])=={p['id'] for p in load(skill/'permissions.json')['grants']}
workflow=load(skill/'workflow.json')['steps']
seen=set()
for step in workflow:
    assert step['id'] not in seen
    assert set(step['depends_on']) <= seen
    seen.add(step['id'])
for path,digest in load(skill/'provenance.json')['source_files'].items():
    assert hashlib.sha256((skill/path).read_bytes()).hexdigest()==digest, path
expected=load(skill/'checksums.json')
actual={p.relative_to(skill).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest() for p in skill.rglob('*') if p.is_file() and p.name not in ['checksums.json','signature.json']}
assert expected==actual, 'Payload integrity mismatch'
# Negative cases protect the contract against common import mistakes.
for field,value in [('entrypoint','../escape'),('entrypoint','C:/escape'),('runtime',{'cloud':False,'android_local':False,'ios_local':False})]:
    invalid=dict(manifest); invalid[field]=value
    assert list(Draft202012Validator(schema).iter_errors(invalid)), field
bad=load(skill/'example-input.json'); bad['language']='hi'
assert list(Draft202012Validator(inputs).iter_errors(bad))
print(f'PASS: manifest/input schemas, 6 styles, DAG, permissions, {len(expected)} payload hashes and negative cases. No rendering or mobile execution was run.')
