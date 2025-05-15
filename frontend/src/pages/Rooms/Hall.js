import HallContent from '../../components/HallContent';
import RoomsLayout from '../../components/RoomsLayout';

export default function LivingRoom() {
    return <RoomsLayout roomName="Hall" ContentComponent={HallContent} />;
};