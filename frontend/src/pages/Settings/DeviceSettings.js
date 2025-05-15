import DeviceSettingsContent from '../../components/DeviceSettingsContent';
import SettingsLayout from '../../components/SettingsLayout';

export default function SettingsProfile() {
    return <SettingsLayout activeInsidePage="Device Settings" InsideContent={<DeviceSettingsContent />} />;
}